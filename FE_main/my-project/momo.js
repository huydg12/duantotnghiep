import pkg from 'body-parser';
import express from 'express';
import axios from 'axios';
import crypto from 'crypto';
const { urlencoded } = pkg;
import config from './config.js';
import cors from 'cors';

const momo = express();
momo.use(express.json());
momo.use(urlencoded({ extended: true }));
momo.use(express.static('./public'));
momo.use(cors({
  origin: 'http://localhost:5173', // Cho phép Vue frontend truy cập
  credentials: true                // Nếu bạn dùng cookie/session
}));
momo.post('/payment', async (req, res) => {
  const {
    amount,        // 💰 Số tiền thanh toán do client truyền lên
    extraData,     // 📦 Dữ liệu phụ nếu cần (mã KH, ID đơn hàng, ... dưới dạng JSON base64)
  } = req.body;

  // Lấy thông tin cố định từ config
  const {
    accessKey,
    secretKey,
    orderInfo,
    partnerCode,
    redirectUrl,
    ipnUrl,
    requestType,
    orderGroupId,
    autoCapture,
    lang,
  } = config;

  const orderId = partnerCode + new Date().getTime();
  const requestId = orderId;

  const rawSignature =
    'accessKey=' + accessKey +
    '&amount=' + amount +
    '&extraData=' + extraData +
    '&ipnUrl=' + ipnUrl +
    '&orderId=' + orderId +
    '&orderInfo=' + orderInfo +
    '&partnerCode=' + partnerCode +
    '&redirectUrl=' + redirectUrl +
    '&requestId=' + requestId +
    '&requestType=' + requestType;

  const signature = crypto
    .createHmac('sha256', secretKey)
    .update(rawSignature)
    .digest('hex');

  const requestBody = JSON.stringify({
    partnerCode,
    partnerName: 'Test',
    storeId: 'MomoTestStore',
    requestId,
    amount,
    orderId,
    orderInfo,
    redirectUrl,
    ipnUrl,
    lang,
    requestType,
    autoCapture,
    extraData,
    orderGroupId,
    signature,
  });

  const options = {
    method: 'POST',
    url: 'https://test-payment.momo.vn/v2/gateway/api/create',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(requestBody),
    },
    data: requestBody,
  };

  try {
    const result = await axios(options);
    return res.status(200).json(result.data); // Trả về payUrl cho client redirect
  } catch (error) {
    return res.status(500).json({ statusCode: 500, message: error.message });
  }
});

momo.post('/callback', async (req, res) => {
  const data = req.body;

  console.log('💬 Callback từ MoMo:', data);

  const {
    amount,
    extraData,
    message,
    orderId,
    orderInfo,
    orderType,
    partnerCode,
    payType,
    requestId,
    responseTime,
    resultCode,
    transId,
    signature
  } = data;

  // ✅ Bước 1: Tính lại signature
  const rawSignature =
    `accessKey=${config.accessKey}` +
    `&amount=${amount}` +
    `&extraData=${extraData}` +
    `&message=${message}` +
    `&orderId=${orderId}` +
    `&orderInfo=${orderInfo}` +
    `&orderType=${orderType}` +
    `&partnerCode=${partnerCode}` +
    `&payType=${payType}` +
    `&requestId=${requestId}` +
    `&responseTime=${responseTime}` +
    `&resultCode=${resultCode}` +
    `&transId=${transId}`;

  const computedSignature = crypto
    .createHmac('sha256', config.secretKey)
    .update(rawSignature)
    .digest('hex');

  // ✅ Bước 2: Kiểm tra chữ ký
  if (computedSignature !== signature) {
    console.error('❌ Signature không hợp lệ.');
    return res.status(400).send('Invalid signature');
  }

  // ✅ Bước 3: Nếu giao dịch thành công
  if (resultCode === 0) {
    console.log(`✅ Giao dịch thành công. Mã đơn: ${orderId}, Số tiền: ${amount}`);
    try {
      // Nếu bạn truyền extraData là chuỗi JSON (ví dụ: thông tin đơn hàng, customerId,...)
      const parsedExtra = extraData ? JSON.parse(Buffer.from(extraData, 'base64').toString()) : {};
      const cartItems = parsedExtra.cartItems || [];
      const billPayload = {
        ...parsedExtra, // nếu client truyền full bill payload vào extraData
        // code: orderId,
        grandTotal: amount,
        statusPayment: "DA_THANH_TOAN",
        billType: "ONLINE",
        ptttId: 2, // MOMO = 2
      };
      // ✅ Gửi về server Java để lưu đơn hàng
      const response = await axios.post("http://localhost:8080/bill/add", billPayload);
      const savedBill = response.data;

      console.log("🔎 Payload gửi về Java:", billPayload);
      console.log("✅ Đã lưu đơn hàng vào CSDL:", savedBill);

      // ✅ Trừ kho dựa vào billDetails
      if (Array.isArray(savedBill.billDetails)) {
        for (const detail of savedBill.billDetails) {
          if (detail.productDetailId && detail.quantity) {
            await axios.put(`http://localhost:8080/inventory/updateQuantityByPayMent/${detail.productDetailId}`, {
              quantity: detail.quantity
            });
          }
        }
      }
        for (const item of cartItems) {
        if (item.cartDetailId) {
            await axios.delete(`http://localhost:8080/cartDetail/delete/${item.cartDetailId}`);
        }
        
        }
    console.log("🔎 Payload gửi về Java:", billPayload);
      console.log("✅ Đã lưu đơn hàng vào CSDL:", response.data);
    } catch (err) {
      console.error("❌ Lỗi khi lưu đơn hàng:", err.message);
    }
  } else {
    console.log(`❌ Giao dịch thất bại. Lý do: ${message}`);
    // TODO: Nếu muốn, có thể ghi log đơn hàng thất bại
  }

  // ✅ Bắt buộc để MoMo không gửi lại callback
  res.status(200).send('OK');
});

// ✅ Check trạng thái giao dịch
momo.post('/check-status-transaction', async (req, res) => {
  const { orderId } = req.body;

  const secretKey = config.secretKey;
  const accessKey = config.accessKey;
  const partnerCode = config.partnerCode;

  const rawSignature = `accessKey=${accessKey}&orderId=${orderId}&partnerCode=${partnerCode}&requestId=${orderId}`;
  const signature = crypto
    .createHmac('sha256', secretKey)
    .update(rawSignature)
    .digest('hex');

  const requestBody = JSON.stringify({
    partnerCode,
    requestId: orderId,
    orderId,
    signature,
    lang: 'vi',
  });

  const options = {
    method: 'POST',
    url: 'https://test-payment.momo.vn/v2/gateway/api/query',
    headers: {
      'Content-Type': 'application/json',
    },
    data: requestBody,
  };

  try {
    const result = await axios(options);
    return res.status(200).json(result.data);
  } catch (error) {
    return res.status(500).json({ statusCode: 500, message: error.message });
  }
});

momo.listen(5000, () => {
  console.log('🚀 Server MoMo chạy tại http://localhost:5000');
});

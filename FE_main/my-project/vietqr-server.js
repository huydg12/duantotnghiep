import express from 'express';
import axios from 'axios';
import cors from 'cors';
// node vietqr-server.js
const app = express();
const PORT = 8081;

app.use(cors());
app.use(express.json());

// Thay thông tin này bằng tài khoản thật
const CLIENT_ID = '9bebd02c-b058-4832-8c34-4b0b7ae62395';
const API_KEY = 'f493f4de-c66e-4e42-809a-ca2a00d4d949';

app.post('/api/generate-qr', async (req, res) => {
  console.log('Dữ liệu nhận từ client:', req.body);

  // 🟢 THÊM acqId vào destructuring
  const { bankCode, accountNo, accountName, amount, addInfo, acqId } = req.body;

  // 🟢 In log dữ liệu gửi đi
  console.log('Dữ liệu gửi đi:', {
    bankCode,
    accountNo,
    accountName,
    amount,
    addInfo,
    acqId
  });

  try {
    const response = await axios.post(
      'https://api.vietqr.io/v2/generate',
      {
        bankCode,
        accountNo,
        accountName,
        amount,
        addInfo,
        template: 'compact',
        acqId, // ✅ dùng biến acqId đã khai báo
      },
      {
        headers: {
          'x-client-id': CLIENT_ID,
          'x-api-key': API_KEY,
        },
      }
    );

    // In log dữ liệu trả về
    console.log('Dữ liệu từ VietQR API:', response.data);
    console.log('Response từ VietQR:', JSON.stringify(response.data, null, 2));

    const qrImage = response.data?.data?.qrDataURL;
    if (!qrImage) {
      return res.status(400).json({ error: 'VietQR API không trả về qrDataURL' });
    }

    res.json({ qrImage });
  } catch (error) {
    console.error('Lỗi tạo mã QR:', error.response?.data || error.message);
    res.status(500).json({
      error: 'Không thể tạo mã QR',
      details: error.response?.data || error.message,
    });
  }
});

app.listen(PORT, () => {
  console.log(`VietQR Server đang chạy tại http://localhost:${PORT}`);
});

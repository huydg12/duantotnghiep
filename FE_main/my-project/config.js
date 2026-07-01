const config = {
    accessKey: process.env.MOMO_ACCESS_KEY,
    secretKey: process.env.MOMO_SECRET_KEY,
    orderInfo: "pay with MoMo",
    partnerCode: "MOMO",
    redirectUrl: "http://localhost:5173/invoicecustomer",
    ipnUrl: process.env.MOMO_IPN_URL,
    requestType: "payWithMethod",
    extraData: "",
    orderGroupId: "",
    autoCapture: true,
    lang: "vi",
};

export default config;

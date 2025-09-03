export async function printReceipt(bill) {
  // bill: { billCode, createdAt, employeeName, customerName, items[], subTotal, discountAmount, grandTotal, paid, change }
  // items[]: { productName, color, size, quantity, price, lineTotal? }

  const html2pdf = (await import("html2pdf.js")).default;

  const safe = (v) => v ?? "";
  const nowText = bill?.createdAt
    ? new Date(bill.createdAt).toLocaleString()
    : new Date().toLocaleString();

  // Tạo DOM tạm để render PDF
  const el = document.createElement("div");
  el.style.width = "500px";
  el.style.fontFamily =
    "Inter, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif";
  el.innerHTML = `
  <div style="padding:16px 14px;">
    <h2 style="margin:0 0 8px 0; font-size:28px; letter-spacing:1px; text-align: center">HÓA ĐƠN BÁN LẺ</h2>
    <div style="font-size:12px; color:#555;">
      <div><b>Mã:</b> ${safe(bill?.billCode)}</div>
      <div><b>Thời gian:</b> ${nowText}</div>
      <div><b>Nhân viên bán hàng:</b> ${safe(bill?.employeeName) || "—"}</div>
      <div><b>Khách hàng:</b> ${safe(bill?.customerName) || "Khách Lẻ"}</div>
      <div><b>Số điện thoại:</b> ${safe(bill?.customerPhone) || "—"}</div>
    </div>

    <hr style="margin:12px 0; border:none; border-top:1px dashed #bbb;" />

    <table style="width:100%; border-collapse:collapse; font-size:12px;">
      <thead>
        <tr>
          <th style="text-align:left; padding:6px 0; border-bottom:1px solid #ddd; width:16px;">#</th>
          <th style="text-align:left; padding:6px 0; border-bottom:1px solid #ddd;">Sản phẩm</th>
          <th style="text-align:center; padding:6px 0; border-bottom:1px solid #ddd; width:24px;">SL</th>
          <th style="text-align:right; padding:6px 0; border-bottom:1px solid #ddd; width:70px;">Đơn giá</th>
          <th style="text-align:right; padding:6px 0; border-bottom:1px solid #ddd; width:80px;">Thành tiền</th>
        </tr>
      </thead>
      <tbody>
        ${(bill?.items || [])
          .map((it, idx) => {
            const name = `${safe(it.productName)}${
              it.color || it.size
                ? " - " +
                  [safe(it.color), safe(it.size)].filter(Boolean).join(" / ")
                : ""
            }`;
            const qty = it?.quantity ?? "";
            const price = it?.price ?? "";
            const total =
              it?.lineTotal != null
                ? it.lineTotal
                : (Number(it?.price) || 0) * (Number(it?.quantity) || 0);
            return `
              <tr>
                <td style="padding:6px 0;">${idx + 1}</td>
                <td style="padding:6px 0;">${name}</td>
                <td style="text-align:center; padding:6px 0;">${qty}</td>
                <td style="text-align:right; padding:6px 0;">${price}</td>
                <td style="text-align:right; padding:6px 0;">${total}</td>
              </tr>`;
          })
          .join("")}
      </tbody>
    </table>

    <hr style="margin:12px 0; border:none; border-top:1px dashed #bbb;" />

    <div style="font-size:13px;">
      <div style="display:flex; justify-content:space-between; margin:4px 0;">
        <span>Tạm tính</span><b>${safe(bill?.subTotal)}</b>
      </div>
      <div style="display:flex; justify-content:space-between; margin:4px 0;">
        <span>Giảm giá</span><b>${safe(bill?.discountAmount)}</b>
      </div>
      <div style="display:flex; justify-content:space-between; margin:6px 0; font-size:15px;">
        <span>Tổng thanh toán</span><b>${safe(bill?.grandTotal)}</b>
      </div>
    </div>

    <div style="text-align:center; margin-top:16px; font-size:12px; color:#666;">
      <div>Cảm ơn quý khách!</div>
      <div>Đổi trả theo chính sách cửa hàng</div>
    </div>
  </div>
  `;

  document.body.appendChild(el);

  const opt = {
    margin: 5,
    filename: `${safe(bill?.billCode) || "hoa-don"}.pdf`,
    image: { type: "jpeg", quality: 0.98 },
    html2canvas: { scale: 2 },
    jsPDF: { unit: "mm", format: "a5", orientation: "portrait" },
    pagebreak: { mode: ["css", "legacy"] },
  };

  await html2pdf()
    .set(opt)
    .from(el)
    .toPdf()
    .get("pdf")
    .then((pdf) => {
      const url = pdf.output("bloburl");
      window.open(url, "_blank"); // mở luôn tab xem hóa đơn
    });

  document.body.removeChild(el);
}

package vn.nbh.paymentservice.enums;

public enum PaymentStatus {
    PENDING,    // Đang chờ khách quét mã
    SUCCESS,    // Đã thanh toán thành công
    FAILED,     // Thanh toán thất bại hoặc hết hạn
    CANCELED    // Bị hủy
}
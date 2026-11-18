# Alo Xe

Ứng dụng Android giúp tài xế không bỏ lỡ tin đặt xe trong nhóm Zalo: đọc thông báo
Zalo ngay trên máy (không đăng nhập hộ, không gửi tin nhắn lên máy chủ nào), lọc theo
từ khóa, rồi bật chuông + màn hình toàn màn hình để nhận chuyến nhanh.

## Lấy file APK để cài thử

1. Vào tab **Actions** của kho này trên GitHub.
2. Bấm vào lần chạy (workflow run) mới nhất có dấu ✅ xanh.
3. Kéo xuống mục **Artifacts**, bấm **alo-xe-debug-apk** để tải file APK về.
4. Chuyển file đó vào điện thoại Android (qua Zalo, email, USB...) và cài đặt
   (máy sẽ hỏi cho phép "cài từ nguồn không xác định" — đồng ý để cài được).

## Cấu trúc chính

- `app/src/main/java/com/aloxe/app/service/ZaloNotificationListener.kt` — nghe thông báo Zalo, lọc từ khóa.
- `app/src/main/java/com/aloxe/app/ui/AlertActivity.kt` — màn hình nhận chuyến (chuông + rung).
- `app/src/main/java/com/aloxe/app/ui/` — các màn hình còn lại (Trang chủ, Cấp quyền, Từ khóa, Chọn nhóm, Lịch sử).
- `.github/workflows/build-apk.yml` — robot tự build APK mỗi khi có code mới.

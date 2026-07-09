# HoloShape Mod - Minecraft Fabric 1.21.1

**Tác giả (Author): HuyPhan(Sun)**

**HoloShape** là một bản Mod Minecraft dạng Client-side (chỉ cần cài ở máy Client) giúp người chơi hiển thị trước hình dáng ảo (Hologram Preview) của các khối hình học 3D trong thế giới Minecraft trước khi tiến hành đặt block thật. Mod hỗ trợ rất nhiều dạng hình học từ cơ bản đến phức tạp, được tính toán động thời gian thực (real-time voxelization) theo hướng nhìn và tọa độ của người chơi.

<p align="center">
  <a href="https://github.com/Huyphan68080/Mod-HoloShape/raw/main/releases/holoshape-1.0.1.jar" target="_blank">
    <img src="https://img.shields.io/badge/T%E1%BA%A3i%20v%E1%BB%81-holoshape--1.0.1.jar-brightgreen?style=for-the-badge&logo=minecraft&logoColor=white" alt="Download JAR" />
  </a>
</p>

---

## ⚙️ Yêu cầu cài đặt (Requirements)

Để mod chạy được, bạn cần chuẩn bị các thành phần sau:
* **Minecraft**: Phiên bản `1.21.1` (hoặc tương thích Fabric Loom).
* **Fabric Loader**: Phiên bản `>=0.16.0`.
* **Fabric API**: Cài đặt bản Fabric API tương thích vào thư mục `mods`.

---

## 🌟 Tính năng nổi bật

* **Hologram Preview thời gian thực**: Xem trước hình dạng của khối trong không gian khi di chuyển chuột một cách mượt mà.
* **Radial Menu trực quan (Mới)**: Giữ phím **H** và di chuột để chọn nhanh hình dạng, nhả phím để chốt hoặc bấm vào tâm để Reset trạng thái. Nhấn nhanh phím **H** để mở bảng cấu hình chi tiết.
* **16 loại hình dạng phong phú (Cập nhật)**: Hỗ trợ đầy đủ các khối cơ bản, hình đa giác, các kết cấu kiến trúc phức tạp và cả công thức toán học tham số.
* **Đùn/Kéo dài hình dạng (Length Extrusion)**: Cho phép kéo dài các mặt 2D dọc theo hướng nhìn (trục X, Y, Z) để tạo ra các đường hầm, cổng vòm dài, hoặc cột trụ một cách nhanh chóng.
* **Hệ thống Chia sẻ Thiết kế (Share Code)**: Sao chép (Export) hoặc Nhập (Import) chuỗi mã hóa Base64 chứa đầy đủ thông tin hình học, phương trình, khối ảo, để chia sẻ trực tiếp với bạn bè hoặc lưu trữ.
* **Tối ưu hóa hiệu năng & Triệt tiêu Lag**:
  * Cơ chế bộ nhớ đệm (caching) tọa độ và bán kính tính toán để tránh dựng lại khi người chơi đứng yên.
  * Sử dụng cấu trúc dữ liệu `LinkedHashSet` cho phép truy xuất/lọc tọa độ trùng lặp với độ phức tạp $O(1)$ thay vì tìm kiếm tuần tự trong danh sách thường.
  * Thuật toán chỉ dựng vỏ ngoài rỗng (Hollow) giúp giảm thiểu số lượng khối ảo cần render, giữ FPS ổn định ngay cả với bán kính cực lớn.

---

## 📐 Danh sách 16 loại hình dạng hỗ trợ

HoloShape hỗ trợ tổng cộng **16 hình dạng** được tối ưu hóa thuật toán tạo vỏ rỗng (hollow):

| Loại hình dạng | Tên hiển thị | Thuật toán toán học & Đặc trưng |
| :--- | :--- | :--- |
| **CIRCLE** | Hình tròn | Thuật toán Midpoint Circle (Bresenham) phẳng 2D. |
| **RECTANGLE** | Hình chữ nhật | Tạo khung chữ nhật phẳng. Đã sửa lỗi hiển thị thiếu cạnh khi chọn chéo hướng âm. |
| **SPHERE** | Hình cầu | Tính khoảng cách Euclidean $x^2 + y^2 + z^2 \approx R^2$ tạo vỏ cầu 3D rỗng. |
| **LINE** | Đường thẳng | Thuật toán Bresenham 3D nối liền điểm 1 và điểm 2. |
| **CUBOID** | Hình hộp | Tạo vỏ hộp chữ nhật rỗng 6 mặt với thuật toán vẽ mặt biên tối ưu. |
| **CYLINDER** | Hình trụ | Vỏ trụ tròn rỗng kèm theo 2 nắp đậy phẳng ở đỉnh và đáy. |
| **CONE** | Hình nón | Đường kính đáy thu hẹp dần tuyến tính theo chiều cao từ đáy lên đỉnh. |
| **PYRAMID** | Hình chóp | Hình chóp tứ giác đều rỗng có thành bên nghiêng dần lên đỉnh. |
| **STAR** | Ngôi sao 3D | Tính tọa độ đỉnh ngôi sao lượng giác 2D, nối chúng bằng Bresenham ở mỗi tầng độ cao. |
| **HEART** | Trái tim 3D | Dựng đường cong trái tim lượng giác theo bán kính với số bước chia động (dynamic steps) giúp viền cong mượt mà, không răng cưa. |
| **TORUS** | Hình phao (Donut) | Phương trình mặt xuyến 3D: $(R - \sqrt{x^2+y^2})^2 + z^2 \approx r^2$. |
| **HEXAGON** | Hình lục giác | Lăng trụ đa giác đều 6 cạnh khép kín bằng các đường nối đỉnh lượng giác. |
| **OCTAGON** | Hình bát giác | Lăng trụ đa giác đều 8 cạnh khép kín bằng các đường nối đỉnh lượng giác. |
| **ARCH** | Cổng vòm (Mới) | Vòm bán nguyệt tròn phía trên kết hợp với hai chân cột thẳng đứng phía dưới. |
| **HORSESHOE** | Móng ngựa (Mới) | Vòm hình móng ngựa phình rộng lượng giác (góc 30 độ) kết hợp chân cột thẳng đứng. |
| **MATH** | Công thức Toán (Mới) | Dựng đường cong 3D tự do từ 3 phương trình tham số $X(t)$, $Y(t)$, $Z(t)$ với bộ phân tích biểu thức toán học tích hợp sẵn (hỗ trợ `sin`, `cos`, `tan`, `sqrt`, `abs`, `pow`, `pi`, `e`). |

---

## 🎮 Hướng dẫn sử dụng trong game

1. **Chọn hình dạng bằng Radial Menu**:
   * Cầm **Xẻng gỗ (Wooden Shovel)** trên tay.
   * **Nhấn giữ phím H**: Màn hình sẽ hiện menu vòng tròn (Radial Menu).
   * **Di chuột** đến phân vùng của hình dạng bạn mong muốn và **nhả phím H** để chọn. 
   * Nếu di chuột vào **Tâm tròn (nút X màu đỏ)** rồi nhả phím, mod sẽ reset trạng thái vẽ hiện tại.
2. **Cấu hình chi tiết (Bảng GUI)**:
   * **Nhấn nhanh phím H** (click rồi thả ngay): Bảng GUI tùy chỉnh chi tiết sẽ hiện lên.
   * Tại đây bạn có thể cấu hình:
     * **Mode**: Dạng khung dây (Lines) hoặc Khối đặc (Solid).
     * **Block**: Thay đổi block ảo hiển thị (kính, thạch anh, đá, gỗ...).
     * **X-Ray**: Bật/Tắt chế độ nhìn xuyên tường của block ảo.
     * **Pulse**: Bật/Tắt hiệu ứng nhấp nháy chuyển màu (Cyan/Blue) sinh động.
     * **Orient**: Hướng xoay cố định (X-Z, Y-Z, X-Y) hoặc tự động theo hướng nhìn (Auto).
     * **Offset**: Căn lề block ảo (Flush - Khớp khối, Inside - Thụt vào trong, Outside - Nhô ra ngoài).
     * **Length**: Độ dài đùn (Extrusion) của hình dạng 2D.
     * **Math Eq**: Khu vực nhập công thức $X(t)$, $Y(t)$, $Z(t)$ và tham số của chúng (chỉ xuất hiện khi chọn shape MATH).
     * **Share Code**: Ô nhập/xuất mã chia sẻ. Bấm **Export** để tự sao chép mã của hình dạng hiện tại vào Clipboard; dán mã vào ô và bấm **Import** để áp dụng thiết kế của người khác.
3. **Xác định Điểm đầu (Điểm 1 - Tâm / Gốc tọa độ)**:
   * Click **Chuột phải** vào một block bất kỳ trong thế giới.
4. **Xem Preview và Chốt Điểm cuối (Điểm 2)**:
   * Di chuyển chuột để xem trước khối dựng ảo xuất hiện trong thế giới thực.
   * Click **Chuột phải** lần nữa để khóa hình dạng cố định (hình hologram sẽ chuyển sang màu xanh lá/xanh lam tùy cấu hình).
5. **Xóa hình dạng**:
   * Nhấn **Sneak (Shift) + Chuột phải** để xóa hoàn toàn hình dạng ảo đã vẽ và đưa trạng thái về mặc định.

---

## 🛠️ Hướng dẫn Build mã nguồn

Dự án sử dụng **Gradle** và **Fabric Loom**:

* **Yêu cầu**: Cài đặt JDK 21.
* **Biên dịch Java**:
  ```powershell
  $env:JAVA_HOME="path\to\jdk21"; .\gradlew.bat compileJava
  ```
* **Đóng gói thành file JAR**:
  ```powershell
  $env:JAVA_HOME="path\to\jdk21"; .\gradlew.bat build
  ```
  File JAR thành phẩm sẽ nằm trong thư mục `build/libs/`.

# HoloShape Mod - Minecraft Fabric 1.21.11
**Cre: HuyPhan(Sun)**

**HoloShape** là một Mod Minecraft dạng Client-side giúp người chơi hiển thị trước hình dáng ảo (Hologram Preview) của các khối hình học 3D trong thế giới thực tế trước khi tiến hành xây dựng hoặc thiết kế. Mod hỗ trợ rất nhiều dạng hình học từ cơ bản đến phức tạp, được tính toán động (real-time voxelization) theo hướng nhìn của người chơi.

<p align="center">
  <a href="https://github.com/Huyphan68080/Mod-HoloShape/raw/main/releases/holoshape-1.0.1.jar" target="_blank">
    <img src="https://img.shields.io/badge/T%E1%BA%A3i%20v%E1%BB%81-holoshape--1.0.1.jar-brightgreen?style=for-the-badge&logo=minecraft&logoColor=white" alt="Download JAR" />
  </a>
</p>

---

## ⚙️ Yêu cầu cài đặt (Requirements)

Để mod chạy được, bạn cần chuẩn bị các phần mềm sau:
* **Fabric Loader**: Phiên bản `>=0.16.0`.
* **Fabric API**: Cài đặt **Fabric API** phiên bản tương thích với Minecraft 1.21.11 trong thư mục `mods` để mod hoạt động bình thường.

---

## 🌟 Tính năng nổi bật
* **Hologram Preview thời gian thực**: Xem trước hình dạng của khối trong không gian khi di chuyển chuột.
* **13 loại hình học phong phú**: Hỗ trợ đầy đủ các khối cơ bản, hình đa giác và cả những hình dạng nghệ thuật phức tạp.
* **Menu trực quan**: Menu lựa chọn dạng lưới 2 cột thông minh giúp thao tác nhanh chóng và không làm tràn màn hình.
* **Tối ưu hóa hiệu năng & Triệt tiêu Lag**: 
  * Cơ chế bộ nhớ đệm (caching) lưu trữ tọa độ con trỏ và hình dạng để tránh tính toán lại khi con trỏ đứng yên.
  * Sử dụng cấu trúc dữ liệu `LinkedHashSet` cho phép truy xuất tọa độ với độ phức tạp $O(1)$ thay vì tìm kiếm tuần tính $O(N)$ trong `List`.
  * Thuật toán vẽ hình hộp 3D tối ưu chỉ dựng bề mặt bao quanh giúp chạy siêu mượt với kích thước cực lớn.

---

## 📐 Danh sách 13 loại hình dạng hỗ trợ

HoloShape hỗ trợ tổng cộng **13 hình dạng**, được phân loại và cài đặt các thuật toán voxelization rỗng (hollow):

| Loại hình dạng | Tên hiển thị | Thuật toán toán học & Đặc trưng |
| :--- | :--- | :--- |
| **CIRCLE** | Hình tròn (2D/3D dẹt) | Sử dụng thuật toán Midpoint Circle (Bresenham) vẽ đường tròn phẳng. |
| **RECTANGLE** | Hình chữ nhật | Tạo khung chữ nhật phẳng. Đã sửa lỗi hiển thị thiếu cạnh khi chọn chéo hướng âm. |
| **SPHERE** | Hình cầu | Tính toán khoảng cách Euclidean $x^2 + y^2 + z^2 \approx R^2$ tạo vỏ cầu rỗng. |
| **LINE** | Đường thẳng | Thuật toán Bresenham 3D nối liền điểm 1 và điểm 2. |
| **CUBOID** | Hình hộp | Tạo vỏ hộp chữ nhật rỗng 6 mặt. Thuật toán đã tối ưu hóa chỉ vẽ diện tích các mặt giúp chống lag hoàn toàn. |
| **CYLINDER** | Hình trụ | Vỏ trụ tròn rỗng kèm theo 2 nắp đậy phẳng ở đỉnh và đáy. |
| **CONE** | Hình nón | Đường kính đáy thu hẹp dần tuyến tính theo chiều cao từ đáy lên đỉnh. |
| **PYRAMID** | Hình chóp | Hình chóp tứ giác đều rỗng có thành bên nghiêng dần lên đỉnh. |
| **STAR** | Ngôi sao 3D | Tính tọa độ đỉnh ngôi sao lượng giác 2D, nối chúng bằng Bresenham ở mỗi tầng độ cao. |
| **HEART** | Trái tim 3D | Dựng đường cong trái tim lượng giác theo bán kính với số bước chia động (dynamic steps) giúp viền cong mượt mà, không răng cưa hay trùng lặp ô ảo. |
| **TORUS** | Hình phao (Donut) | Phương trình mặt xuyến 3D: $(R - \sqrt{x^2+y^2})^2 + z^2 \approx r^2$. |
| **HEXAGON** | Hình lục giác | Lăng trụ đa giác đều 6 cạnh khép kín bằng các đường nối đỉnh lượng giác. |
| **OCTAGON** | Hình bát giác | Lăng trụ đa giác đều 8 cạnh khép kín bằng các đường nối đỉnh lượng giác. |

---

## 🎮 Hướng dẫn sử dụng trong game

1. **Chọn hình dạng**: 
   * Cầm **Xẻng gỗ (Wooden Shovel)** trên tay.
   * Nhấn phím **H** để mở menu GUI. Chọn hình dạng bạn muốn vẽ.
2. **Xác định điểm đầu (Điểm 1 - Khối mẫu)**:
   * Click **Chuột phải** vào một khối trong thế giới. Khối này sẽ được lấy làm **khối mẫu** để đối chiếu xây dựng.
3. **Xem Preview và Chốt điểm cuối (Điểm 2)**:
   * Di chuyển chuột để xem trước khối dựng ảo xuất hiện trong thế giới thực.
   * Click **Chuột phải** lần nữa để khóa hình dạng cố định (chuyển sang màu xanh Cyan).
4. **Xóa hình dạng**:
   * Nhấn **Sneak (Shift) + Chuột phải** để xóa hình dạng đã vẽ.

---

## 🛠️ Hướng dẫn Build mã nguồn

Dự án sử dụng **Gradle** và **Fabric Loom**:

* **Yêu cầu**: Cài đặt JDK 21 (Có thể cài qua Scoop: `scoop bucket add java` -> `scoop install openjdk21`).
* **Biên dịch Java**:
  ```powershell
  $env:JAVA_HOME="path\to\jdk21"; .\gradlew.bat compileJava
  ```
* **Đóng gói thành file JAR**:
  ```powershell
  $env:JAVA_HOME="path\to\jdk21"; .\gradlew.bat build
  ```

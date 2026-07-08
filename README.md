# HoloShape Mod - Minecraft Fabric 1.21.11

**HoloShape** là một Mod Minecraft dạng Client-side giúp người chơi hiển thị trước hình dáng ảo (Hologram Preview) của các khối hình học 3D trong thế giới thực tế trước khi tiến hành xây dựng hoặc thiết kế. Mod hỗ trợ rất nhiều dạng hình học từ cơ bản đến phức tạp, được tính toán động (real-time voxelization) theo hướng nhìn của người chơi.

<p align="center">
  <a href="https://github.com/Huyphan68080/Mod-HoloShape/raw/main/releases/holoshape-1.0.0.jar" target="_blank">
    <img src="https://img.shields.io/badge/T%E1%BA%A3i%20v%E1%BB%81-holoshape--1.0.0.jar-brightgreen?style=for-the-badge&logo=minecraft&logoColor=white" alt="Download JAR" />
  </a>
</p>

---

## 🌟 Tính năng nổi bật
* **Hologram Preview thời gian thực**: Xem trước hình dạng của khối trong không gian khi di chuyển chuột.
* **13 loại hình học phong phú**: Hỗ trợ đầy đủ các khối cơ bản, hình đa giác và cả những hình dạng nghệ thuật phức tạp.
* **Giao diện Menu trực quan**: Menu lựa chọn dạng lưới 2 cột thông minh giúp thao tác nhanh chóng và không làm tràn màn hình.
* **Tối ưu hóa hiệu năng**: Sử dụng cơ chế lưu trữ tạm (cache) chiều cao và bán kính để tránh tính toán lại liên tục khi không cần thiết.

---

## 📐 Danh sách 13 loại hình dạng hỗ trợ

HoloShape hỗ trợ tổng cộng **13 hình dạng**, được phân loại và cài đặt các thuật toán voxelization rỗng (hollow):

| Loại hình dạng | Tên hiển thị | Thuật toán toán học & Đặc trưng |
| :--- | :--- | :--- |
| **CIRCLE** | Hình tròn (2D/3D dẹt) | Sử dụng thuật toán Midpoint Circle (Bresenham) vẽ đường tròn phẳng. |
| **RECTANGLE** | Hình chữ nhật | Tạo khung chữ nhật rỗng trong không gian phẳng. |
| **SPHERE** | Hình cầu | Tính toán khoảng cách Euclidean $x^2 + y^2 + z^2 \approx R^2$ tạo vỏ cầu rỗng. |
| **LINE** | Đường thẳng | Thuật toán Bresenham 3D nối liền điểm 1 và điểm 2. |
| **CUBOID** | Hình hộp | Tạo vỏ hộp chữ nhật rỗng 6 mặt (trên, dưới, trước, sau, trái, phải). |
| **CYLINDER** | Hình trụ | Vỏ trụ tròn rỗng kèm theo 2 nắp đậy phẳng ở đỉnh và đáy. |
| **CONE** | Hình nón | Đường kính đáy thu hẹp dần tuyến tính theo chiều cao từ đáy lên đỉnh. |
| **PYRAMID** | Hình chóp | Hình chóp tứ giác đều rỗng có thành bên nghiêng dần lên đỉnh. |
| **STAR** | Ngôi sao 3D | Tính tọa độ 5 đỉnh nhọn và 5 đỉnh lõm lượng giác 2D, nối chúng bằng Bresenham ở mỗi tầng độ cao. |
| **HEART** | Trái tim 3D | Dựng đồ thị phương trình trái tim lượng giác 2D ở mỗi tầng độ cao để tạo lăng trụ trái tim 3D. |
| **TORUS** | Hình phao (Donut) | Phương trình mặt xuyến 3D: $(R - \sqrt{x^2+y^2})^2 + z^2 \approx r^2$. |
| **HEXAGON** | Hình lục giác | Lăng trụ đa giác đều 6 cạnh khép kín bằng các đường nối đỉnh lượng giác. |
| **OCTAGON** | Hình bát giác | Lăng trụ đa giác đều 8 cạnh khép kín bằng các đường nối đỉnh lượng giác. |

---

## 🎮 Hướng dẫn sử dụng trong game

1. **Chọn hình dạng**: 
   * Cầm **Xẻng gỗ (Wooden Shovel)** trên tay.
   * Nhấn phím **H** để mở menu GUI. Chọn hình dạng bạn muốn vẽ từ menu lưới 2 cột.
2. **Xác định điểm đầu (Điểm 1 - Góc/Tâm)**:
   * Click **Chuột phải** vào một khối bất kỳ trong thế giới để đặt Điểm 1.
3. **Xem Preview và Chốt điểm cuối (Điểm 2)**:
   * Di chuyển chuột để xem trước khối dựng ảo xuất hiện trong thế giới thực.
   * Click **Chuột phải** lần nữa để khóa hình dạng (chuyển từ màu đỏ sang màu xanh Cyan).
4. **Xóa hình dạng**:
   * Nhấn **Sneak (Shift) + Chuột phải** để xóa hình dạng đã vẽ.

---

## 🛠️ Hướng dẫn Build mã nguồn

Dự án sử dụng **Gradle** và **Fabric Loom**:

* **Yêu cầu**: Cài đặt JDK 21 và cấu hình biến môi trường `JAVA_HOME`.
* **Biên dịch Java**:
  ```powershell
  $env:JAVA_HOME=\"path\to\jdk21\"; .\gradlew.bat compileJava
  ```
* **Đóng gói thành file JAR**:
  ```powershell
  $env:JAVA_HOME=\"path\to\jdk21\"; .\gradlew.bat build
  ```

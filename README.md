# OOP Eco Simulator

Đây là project mô phỏng hệ sinh thái 2D bằng Java Swing. Game đọc map từ file Tiled `.tmx`, spawn động vật, xử lý di chuyển, săn mồi, ăn cỏ, vùng nước, collision và render sprite animation.

## Cách Chạy

Chạy từ thư mục gốc của project:

```bash
cd "/Users/mac/OOP/OOP_EcoSimulator"
javac -d bin $(find src -name "*.java")
java -cp bin main.Main
```

Lệnh `javac -d bin ...` compile toàn bộ file `.java` trong `src` vào thư mục `bin`.

Lệnh `java -cp bin main.Main` mở cửa sổ mô phỏng. Entry point nằm ở:

```text
src/main/Main.java
```

## Luồng Chạy Chính

### `Main`

File: `src/main/Main.java`

`Main.main()` tạo cửa sổ Swing:

- Tạo `JFrame` kích thước `800x600`.
- Tạo `SimulationPanel` để render map và entity.
- Tạo `SimulationEngine`.
- Gọi `engine.start()` để bắt đầu vòng lặp mô phỏng.

### `SimulationEngine`

File: `src/controller/SimulationEngine.java`

`SimulationEngine` là controller chính:

- Tạo `Jungle` theo kích thước map đang render.
- Gọi `Environment.setActiveEnvironment(env)` để các strategy có thể truy cập môi trường hiện tại.
- Chạy game loop trong thread riêng.
- Mỗi frame gọi `env.update()`.
- Tạo snapshot entity rồi gọi `panel.repaint()`.
- Sleep khoảng `16ms` để giữ nhịp gần `60 FPS`.

## Render Map Và Camera

### `SimulationPanel`

File: `src/view/SimulationPanel.java`

Panel chịu trách nhiệm render:

- Load map từ `assets/Environment/Forest/Forest.tmx`.
- Render tile map thông qua `ForestTileMap`.
- Render động vật theo sprite sheet.
- Render thanh HP phía trên mỗi con vật.
- Camera di chuyển theo chuột nếu map lớn hơn màn hình.

Các sprite hiện đang được load:

- `Hare`
- `Fox`
- `Boar`
- `Deer`
- `BlackGrouse`
- `Fish1_animation`
- `Fish2_animation`
- `Fish3_animation`
- `Fish4_animation`
- `Chicken_animation`
- `Cow_animation`
- `Pig_animation`

Riêng `Chicken`, `Cow`, `Pig` dùng 4 frame đầu tiên của hàng đầu tiên:

```text
frame 0: đi xuống
frame 1: đi lên
frame 2: đi trái
frame 3: đi phải
```

Ba loài này không chạy animation nhiều frame, vì sprite sheet hiện tại xoay nhiều hướng trong cùng một sheet.

### `ForestTileMap`

File: `src/view/ForestTileMap.java`

Class này đọc file `.tmx` và render các tile layer:

- Đọc tileset nội bộ và external `.tsx`.
- Hỗ trợ infinite map bằng cách đọc các `chunk`.
- Tính kích thước map theo tile thật.
- Hỗ trợ các tile bị flip hoặc rotate trong Tiled.
- Vẽ toàn bộ tile map thành `BufferedImage` rồi render lên màn hình.

## Layer Logic Trong Tiled

File map chính:

```text
assets/Environment/Forest/Forest.tmx
```

Các layer logic đang được code đọc theo tên:

```text
map_bounds
collision
water_zone
coop
cowshed
pigsty
wicket
```

### `map_bounds`

Layer này định nghĩa vùng map hợp lệ. Động vật không được đi ra ngoài vùng này.

Nên có một object lớn bao phủ toàn bộ vùng chơi.

### `collision`

Layer này chứa vật cản cứng:

- Cây
- Đá
- Gốc cây
- Object trang trí nhưng chặn đường

Mọi động vật sẽ bị đẩy ra khỏi các collider trong layer này.

### `water_zone`

Layer này định nghĩa vùng nước.

Hiện tại chỉ cá được vào nước. Các loài khác nếu đi vào `water_zone` sẽ bị trả về vị trí trước đó.

`water_zone` có thể dùng polygon. Code đã hỗ trợ:

- Rectangle
- Ellipse
- Polygon

### `coop`, `cowshed`, `pigsty`

Đây là các object layer dùng làm chuồng nuôi:

- `coop`: chuồng gà.
- `cowshed`: chuồng bò.
- `pigsty`: chuồng lợn.

Các loài vật nuôi chỉ spawn trong đúng layer chuồng của nó:

- `Chicken` chỉ spawn trong `coop`.
- `Cow` chỉ spawn trong `cowshed`.
- `Pig` chỉ spawn trong `pigsty`.

Trong quá trình update, vật nuôi bị giữ lại trong đúng vùng chuồng. Nếu chúng đi ra ngoài polygon chuồng, hệ thống trả chúng về vị trí trước đó.

### `wicket`

Layer này dùng cho hàng rào/cửa chắn chuồng.

`wicket` được xử lý như collider riêng:

- Các loài bên ngoài bị chặn, không đi xuyên qua wicket.
- `Chicken`, `Cow`, `Pig` được bỏ qua wicket để có thể spawn và đi lại bên trong chuồng.

## Đọc Collision Từ TMX

### `TmxCollisionLoader`

File: `src/model/environment/TmxCollisionLoader.java`

Class này đọc object layer trong `.tmx`:

- `map_bounds` -> lưu thành giới hạn map.
- `collision` -> lưu thành danh sách vật cản.
- `water_zone` -> lưu thành danh sách vùng nước.

Vì map Tiled có thể dùng chunk âm, loader sẽ tự dịch tọa độ object theo offset của tile map để collider khớp với hình ảnh render.

### `MapCollider`

File: `src/model/environment/MapCollider.java`

Class này biểu diễn collider:

- `RECTANGLE`
- `ELLIPSE`
- `POLYGON`

Các hàm chính:

- `containsCircle(...)`: kiểm tra một entity dạng hình tròn có nằm trong collider không.
- `clampCircleInside(...)`: giữ entity nằm bên trong vùng cho phép.
- `resolveCircleCollision(...)`: đẩy entity ra khỏi vật cản.
- `randomPointInside(...)`: lấy vị trí ngẫu nhiên bên trong collider.
- `getArea()`: lấy diện tích collider, dùng để ưu tiên spawn cá ở vùng nước rộng.

## Môi Trường

### `Environment`

File: `src/model/environment/Environment.java`

Đây là class nền cho map/môi trường. Hàm quan trọng nhất là:

```java
public void update()
```

Mỗi frame, `update()` làm các bước:

1. Tạo lại `QuadTree` để tối ưu truy vấn entity gần nhau.
2. Gọi `entity.update()` cho từng entity.
3. Chặn entity ra khỏi biên map bằng `keepWithinBounds(...)`.
4. Kiểm tra nước bằng `resolveWaterAccess(...)`.
5. Kiểm tra va chạm map bằng `resolveMapCollisions(...)`.
6. Gọi `CollisionHandler.processCollisions(...)` để xử lý entity đụng nhau.
7. Xóa entity chết khỏi danh sách.

Các hàm spawn vị trí:

- `randomOpenPosition(...)`: spawn trên đất, tránh nước và vật cản.
- `randomWaterPosition(...)`: spawn trong nước, ưu tiên vùng nước rộng.

### `Jungle`

File: `src/model/environment/Map/Jungle.java`

`Jungle` là map hiện đang dùng. Khi khởi tạo, class này:

- Load collision từ `Forest.tmx`.
- Spawn thú ăn cỏ.
- Spawn thú ăn thịt.
- Spawn cá.
- Spawn vật nuôi trong chuồng.
- Spawn grass/bush dùng cho logic ăn uống và trú ẩn.

Số lượng spawn hiện tại:

- `Rabbit`: 18
- `BlackGrouse`: 14
- `Deer`: 8
- `Boar`: 6
- `Wolf`: 5
- `Fox`: 4
- Mỗi loại cá: 8
- `Chicken`: 4
- `Cow`: 2
- `Pig`: 3
- `Grass`: 90
- `Bush`: 22

## Entity Và Animal

### `Entity`

File: `src/model/Entity.java`

`Entity` là class gốc cho mọi object có vị trí:

- `position`
- `size`
- `isAlive`
- `id`

Mọi entity đều có hàm:

```java
public abstract void update();
```

### `Animal`

File: `src/model/Animal.java`

`Animal` là class nền cho các loài động vật.

Các chỉ số chính:

- `hp`: máu hiện tại.
- `maxHp`: máu tối đa.
- `energy`: năng lượng hiện tại.
- `maxEnergy`: năng lượng tối đa.
- `speed`: tốc độ.
- `visionRadius`: tầm nhìn.
- `velocity`: vận tốc.
- `currentState`: trạng thái hiện tại.
- `canEnterWater`: có được vào nước không.
- `requiresWater`: có bắt buộc phải ở trong nước không.

Các trạng thái quan trọng:

- `WANDERING`: đi lang thang.
- `CHASING`: săn đuổi.
- `FLEEING`: bỏ chạy.
- `FORAGING`: tìm thức ăn.
- `EATING`: đang ăn.
- `HIDING`: đang trốn.
- `ATTACKING`: đang tấn công.
- `HURT`: bị thương.
- `DEAD`: chết.

Mỗi frame, `Animal.update()`:

1. Giảm năng lượng.
2. Tăng tuổi.
3. Kiểm tra chết vì hết máu, hết năng lượng hoặc quá già.
4. Gọi `brain.execute(this)` nếu đang không bị khóa state.
5. Cộng `velocity` vào `position`.

`stateLockTicks` dùng để giữ các state ngắn như `ATTACKING`, `HURT`, `DEAD` trong vài frame, tránh bị strategy đổi state ngay lập tức.

## Strategy Logic

Các hành vi động vật đang dùng Strategy Pattern. Mỗi con vật có một `brain`.

### `PassiveStrategy`

File: `src/model/strategy/PassiveStrategy.java`

Dùng cho hành vi đi dạo:

- Có lúc đứng yên nghỉ.
- Có lúc chọn hướng đi ngẫu nhiên.
- Giữ hướng trong một khoảng thời gian thay vì đổi liên tục.
- Tự né gần mép map.

### `ForagingStrategy`

File: `src/model/strategy/ForagingStrategy.java`

Dùng cho thú ăn cỏ:

1. Tìm thú ăn thịt gần nhất.
2. Nếu thấy nguy hiểm, chuyển sang `FLEEING` và chạy ra xa.
3. Nếu đói, tìm `Grass` gần nhất.
4. Nếu tìm được cỏ, chuyển sang `FORAGING` và đi tới cỏ.
5. Nếu không có gì cần làm, dùng `PassiveStrategy`.

### `HunterStrategy`

File: `src/model/strategy/HunterStrategy.java`

Dùng cho thú ăn thịt:

1. Nếu đói, tìm con mồi trong `preyDetectionRadius`.
2. Nếu không quá đói, chỉ săn khi con mồi ở rất gần.
3. Nếu thấy mồi, chuyển sang `CHASING` và chạy tới.
4. Nếu đói mà không thấy mồi, có thể tìm `Carcass`.
5. Nếu không có mục tiêu, dùng `PassiveStrategy`.

## Logic Thú Ăn Thịt

### `Carnivore`

File: `src/model/carnivore/Carnivore.java`

Thú ăn thịt có thêm:

- `attackDamage`: sát thương mỗi lần cắn.
- `attackCooldown`: thời gian chờ giữa các lần cắn.
- `preyDetectionRadius`: phạm vi phát hiện con mồi.
- `preyTypes`: danh sách loài có thể săn.

Hàm quan trọng:

- `attack(Animal prey)`: gây damage cho con mồi.
- `canAttack(Animal prey)`: kiểm tra con mồi có nằm trong danh sách được phép săn không.

### `Fox`

File: `src/model/carnivore/Fox.java`

Cáo hiện chỉ săn:

- `Rabbit`
- `BlackGrouse`

Nó không săn mọi loài ăn cỏ. Luật này nằm trong `preyTypes`.

### `Wolf`

File: `src/model/carnivore/Wolf.java`

Sói dùng `HunterStrategy`. Vì hiện chưa giới hạn `preyTypes`, nó có thể săn các loài `Herbivore` hợp lệ.

## Logic Thú Ăn Cỏ

### `Herbivore`

File: `src/model/herbivore/Herbivore.java`

Thú ăn cỏ có thêm:

- `predatorDetectionRadius`: phạm vi phát hiện thú săn mồi.

Các loài hiện đang spawn:

- `Rabbit`
- `BlackGrouse`
- `Deer`
- `Boar`

Chúng dùng `ForagingStrategy`, nghĩa là sẽ ưu tiên chạy trốn khi thấy thú ăn thịt, sau đó mới tìm cỏ nếu đói.

## Logic Cá Và Vùng Nước

### `Fish`

File: `src/model/aquatic/Fish.java`

Cá là `Animal` sống dưới nước:

```java
this.canEnterWater = true;
this.requiresWater = true;
```

Điều này có nghĩa là:

- Cá được phép vào `water_zone`.
- Cá bị giữ lại trong `water_zone`.
- Cá không spawn trên đất.

Các loài cá hiện có:

- `FishOne`
- `FishTwo`
- `FishThree`
- `FishFour`

Vùng spawn cá được chọn bằng `randomWaterPosition(...)`. Hàm này ưu tiên vùng nước rộng bằng cách chọn `water_zone` theo diện tích.

## Logic Vật Nuôi Và Chuồng

### `DomesticAnimal`

File: `src/model/domestic/DomesticAnimal.java`

`DomesticAnimal` là class nền cho vật nuôi. Mỗi vật nuôi có một `penLayerName` để biết nó thuộc chuồng nào.

Các loài hiện có:

- `Chicken`: thuộc layer `coop`.
- `Cow`: thuộc layer `cowshed`.
- `Pig`: thuộc layer `pigsty`.

Các loài này dùng `PassiveStrategy`, di chuyển chậm và có lúc đứng yên. Tốc độ hiện tại:

- `Chicken`: `1.1`
- `Cow`: `0.7`
- `Pig`: `0.9`

Vị trí spawn dùng `randomPenPosition(...)`, nghĩa là hệ thống chỉ lấy điểm ngẫu nhiên bên trong đúng polygon chuồng của loài đó.

`wicket` không được tính là vật cản đối với vật nuôi trong chuồng, nhưng vẫn là vật cản đối với các loài khác.

## Va Chạm Entity

### `CollisionHandler`

File: `src/controller/CollisionHandler.java`

Class này xử lý va chạm giữa entity với entity:

- `Carnivore` đụng `Herbivore` -> tấn công.
- `Herbivore` đụng `Eatable` -> ăn.
- `Carnivore` đụng `Carcass` -> ăn xác.
- `Animal` đụng `Obstacle` -> bị cản hoặc trốn nếu object là `Hideable`.

Nếu thú ăn thịt giết được con mồi, hệ thống tạo `Carcass` tại vị trí con mồi.

## Quy Tắc Làm Map Trong Tiled

Khi chỉnh `Forest.tmx`, cần giữ đúng tên layer:

```text
map_bounds
collision
water_zone
coop
cowshed
pigsty
wicket
```

Nếu muốn thêm vật cản cứng, vẽ object vào `collision`.

Nếu muốn thêm vùng nước, vẽ object vào `water_zone`. Nên dùng polygon để bao chính xác hình dạng ao/hồ/sông.

Nếu muốn thêm chuồng, vẽ polygon vào đúng layer:

- Gà: `coop`
- Bò: `cowshed`
- Lợn: `pigsty`

Nếu muốn thêm hàng rào/cửa chắn chuồng, vẽ object vào `wicket`.

Nếu muốn thay đổi biên map, sửa object trong `map_bounds`.

Sau khi chỉnh map trong Tiled, nhớ save lại `Forest.tmx`, compile và chạy lại game.

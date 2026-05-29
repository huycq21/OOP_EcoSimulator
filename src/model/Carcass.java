package model;

// Xác chết cũng là một vật thể trên bản đồ, ăn được (Eatable) và sẽ thối rữa (Ageable)
public class Carcass extends Entity implements Eatable, Ageable {
    private double meatLeft; // Lượng thịt (năng lượng) còn lại
    private int age;         // Thời gian đã phân hủy
    private int maxAge;      // Thời gian tối đa trước khi thối rữa hoàn toàn (biến mất)

    public Carcass(Vector2D position, double size, double totalMeat) {
        super(position, size);
        this.meatLeft = totalMeat;
        this.age = 0;
        this.maxAge = 3000; // Tương đương khoảng 48 giây (nếu 1 tick = 16ms), sau đó xác tự biến mất
    }

    @Override
    public void update() {
        if (!isAlive) return;

        // Xác chết thối rữa theo thời gian
        growOlder();

        // Nếu hết hạn sử dụng HOẶC bị cắn hết sạch thịt -> Xóa sổ khỏi bản đồ
        if (isTooOld() || meatLeft <= 0) {
            this.destroy();
        }
    }

    // --- HÀM ĐẶC BIỆT DÀNH CHO THÚ ĂN THỊT ---
    // Khác với Cỏ (ăn 1 phát là mất), Xác chết cho phép cắn từng miếng.
    // Thú ăn thịt truyền vào kích cỡ miệng (biteSize), hàm này trả về lượng thịt thực sự cắn được.
    public double takeBite(double biteSize) {
        if (meatLeft >= biteSize) {
            meatLeft -= biteSize;
            return biteSize; // Cắn ngập mồm
        } else {
            double remaining = meatLeft;
            meatLeft = 0;
            this.destroy(); // Miếng thịt cuối cùng đã bị ăn hết
            return remaining; // Chỉ vớt vát được chút thịt vụn còn lại
        }
    }

    // --- Triển khai Eatable ---
    @Override
    public double getEnergyValue() {
        return meatLeft;
    }

    @Override
    public void getEaten() {
        this.meatLeft = 0;
        this.destroy();
    }

    // --- Triển khai Ageable ---
    @Override
    public void growOlder() {
        this.age++;
    }

    @Override
    public boolean isTooOld() {
        return this.age >= this.maxAge;
    }
}
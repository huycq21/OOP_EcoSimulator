package util;

import javax.sound.sampled.*;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SoundManager {

    private static final String SOUND_PATH = "assets/sounds/";
    
    // --- CƠ CHẾ QUẢN LÝ LUỒNG ÂM THANH (POLYPHONY) ---
    private static final int MAX_OVERLAP = 3; // Số lượng âm thanh đè lên nhau tối đa
    private static final ConcurrentHashMap<String, AtomicInteger> soundCounts = new ConcurrentHashMap<>();

    public static void playSound(String fileName) {
        // 1. Lấy bộ đếm của file âm thanh này. Nếu chưa có thì tạo mới bộ đếm bắt đầu từ 0.
        soundCounts.putIfAbsent(fileName, new AtomicInteger(0));
        AtomicInteger currentCount = soundCounts.get(fileName);

        // 2. Kiểm tra giới hạn: Nếu đang có 3 cái cùng loại đang phát rồi thì TỪ CHỐI phát thêm!
        if (currentCount.get() >= MAX_OVERLAP) {
            return; 
        }

        try {
            File file = new File(SOUND_PATH + fileName);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            
            // 3. Đã duyệt cho phát -> Tăng bộ đếm lên 1
            currentCount.incrementAndGet();
            
            // 4. LẮNG NGHE SỰ KIỆN ĐỂ DỌN DẸP TRÊN LUỒNG CHẠY (Bản mới)
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    // Âm thanh phát xong -> Giảm bộ đếm xuống 1 để nhường chỗ cho âm thanh khác
                    currentCount.decrementAndGet(); 
                    
                    // Tự sát giải phóng RAM
                    clip.close();
                    try {
                        audioStream.close(); 
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            clip.open(audioStream);
            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Clip playLoop(String fileName) {
        try {
            File file = new File(SOUND_PATH + fileName);

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            clip.loop(Clip.LOOP_CONTINUOUSLY);

            return clip;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void stopSound(Clip clip) {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
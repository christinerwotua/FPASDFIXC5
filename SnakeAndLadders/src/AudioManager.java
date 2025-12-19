import javax.sound.sampled.*;
import java.io.File;

public class AudioManager {
    private Clip bgmClip;
    private Clip sfxClip;
    private Clip walkClip;
    private Clip winClip;

    private boolean muted = false;
    private int bgmVolume = 50;

    private String lastBgmFile = null;

    public boolean isMuted() {
        return muted;
    }

    public void setBgmVolume(int sliderValue) {
        bgmVolume = sliderValue;
        if (bgmClip != null && bgmClip.isOpen() && !muted) {
            setClipVolume(bgmClip, bgmVolume);
        }
    }

    public void toggleMuteAll() {
        muted = !muted;

        if (muted) {
            // stop everything
            stopMusic();
            stopSfxNow();
            stopWalkSound();
            stopWinSound();
        } else {
            // kalau sebelumnya ada BGM terakhir, nyalakan lagi
            if (lastBgmFile != null) {
                playBackgroundMusic(lastBgmFile);
            }
        }
    }

    public void playBackgroundMusic(String filePath) {
        lastBgmFile = filePath;
        if (muted) return;

        try {
            File musicPath = resolveFile(filePath);
            if (!musicPath.exists()) return;

            stopMusic();

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioInput);

            setClipVolume(bgmClip, bgmVolume);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception ignored) {}
    }

    public void playEffect(String filePath) {
        if (muted) return;

        try {
            File sfxPath = resolveFile(filePath);
            if (!sfxPath.exists()) return;

            if (sfxClip != null && sfxClip.isRunning()) { sfxClip.stop(); sfxClip.close(); }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(sfxPath);
            sfxClip = AudioSystem.getClip();
            sfxClip.open(audioInput);

            // volume effect fixed (boleh kamu ubah)
            setClipVolume(sfxClip, 100);
            sfxClip.start();
        } catch (Exception ignored) {}
    }

    public void playWalkSound() {
        if (muted) return;

        try {
            File walkPath = resolveFile("walk.wav");
            if (!walkPath.exists()) return;

            stopWalkSound();

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(walkPath);
            walkClip = AudioSystem.getClip();
            walkClip.open(audioInput);

            setClipVolume(walkClip, 90);
            walkClip.loop(Clip.LOOP_CONTINUOUSLY);
            walkClip.start();
        } catch (Exception ignored) {}
    }

    public void stopWalkSound() {
        if (walkClip != null) {
            try {
                if (walkClip.isRunning()) walkClip.stop();
                walkClip.close();
            } catch (Exception ignored) {}
        }
    }

    public void playWinSound() {
        if (muted) return;

        try {
            File winPath = resolveFile("win.wav");
            if (!winPath.exists()) return;

            stopWinSound();

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(winPath);
            winClip = AudioSystem.getClip();
            winClip.open(audioInput);

            setClipVolume(winClip, 100);
            winClip.start();
        } catch (Exception ignored) {}
    }

    private void stopWinSound() {
        if (winClip != null) {
            try {
                if (winClip.isRunning()) winClip.stop();
                winClip.close();
            } catch (Exception ignored) {}
        }
    }

    public void stopMusic() {
        if (bgmClip != null) {
            try {
                if (bgmClip.isRunning()) bgmClip.stop();
                bgmClip.close();
            } catch (Exception ignored) {}
        }
    }

    private void stopSfxNow() {
        if (sfxClip != null) {
            try {
                if (sfxClip.isRunning()) sfxClip.stop();
                sfxClip.close();
            } catch (Exception ignored) {}
        }
    }

    private void setClipVolume(Clip clip, int sliderValue) {
        if (clip != null && clip.isOpen()) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float maxVol = gainControl.getMaximum();
                float minVol = gainControl.getMinimum();

                float db;
                if (sliderValue <= 0) db = minVol;
                else if (sliderValue > 100) db = maxVol;
                else db = (float) (6.0f + 20.0f * Math.log10(sliderValue / 50.0));

                if (db > maxVol) db = maxVol;
                if (db < minVol) db = minVol;

                gainControl.setValue(db);
            } catch (Exception ignored) {}
        }
    }

    private File resolveFile(String fileName) {
        File f = new File(fileName);
        if (!f.exists()) f = new File(System.getProperty("user.dir"), fileName);
        return f;
    }
}

package moe.haruue.redrockexam.musicplayer.ui.service;

import android.media.MediaPlayer;

import java.util.ArrayList;

import moe.haruue.redrockexam.musicplayer.R;
import moe.haruue.redrockexam.musicplayer.data.model.SongModel;
import moe.haruue.redrockexam.musicplayer.data.storage.CurrentPlay;
import moe.haruue.redrockexam.musicplayer.data.storage.CurrentPlayList;
import moe.haruue.redrockexam.musicplayer.util.LocalUtils;
import moe.haruue.redrockexam.util.StandardUtils;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
public class MusicPlayerController {

    static Listener listener = new Listener();

    public static void play() {
        if (MusicPlayServiceConnection.getMediaPlayer().isPlaying()) {
            return;
        }
        if (CurrentPlay.instance.data != null) {
            MusicPlayServiceConnection.getMediaPlayer().start();
            notifyCurrentPlayMusicChange();
        } else {
            if (CurrentPlayList.hasList()) {
                play(CurrentPlayList.random());
            } else {
                toastNoList();
            }
        }
    }

    public static void pause() {
        if (!MusicPlayServiceConnection.getMediaPlayer().isPlaying()) {
            return;
        }
        MusicPlayServiceConnection.getMediaPlayer().pause();
        notifyCurrentPlayMusicChange();
    }

    public static void stop() {
        if (!MusicPlayServiceConnection.getMediaPlayer().isPlaying()) {
            return;
        }
        MusicPlayServiceConnection.getMediaPlayer().stop();
        notifyCurrentPlayMusicChange();
    }

    public static void seekTo(int msec) {
        MusicPlayServiceConnection.getMediaPlayer().seekTo(msec);
    }

    public static void play(SongModel song) {
        try {
            StandardUtils.log("song.file: " + song.file);
            StandardUtils.log("song.m4aCache: " + song.m4aCache);
            StandardUtils.log("song.m4aUrl: " + song.m4aUrl);
            if (CurrentPlay.instance.data != null) {
                if (CurrentPlay.instance.data.equals(song)) {
                    MusicPlayServiceConnection.getMediaPlayer().seekTo(0);
                    return;
                } else {
                    MusicPlayServiceConnection.getMediaPlayer().stop();
                    MusicPlayServiceConnection.getMediaPlayer().reset();
                }
            }
            if (LocalUtils.isLocal(song)) {
                MusicPlayServiceConnection.getMediaPlayer().setDataSource(song.file);
            } else {
                MusicPlayServiceConnection.getMediaPlayer().setDataSource(song.m4aUrl);
            }
            MusicPlayServiceConnection.getMediaPlayer().setOnCompletionListener(listener);
            MusicPlayServiceConnection.getMediaPlayer().setOnPreparedListener(listener);
            MusicPlayServiceConnection.getMediaPlayer().prepareAsync();
            CurrentPlay.instance.data = song;
        } catch (Exception e) {
            StandardUtils.printStack(e);
            StandardUtils.toast(R.string.load_error_listen_other);
        }
    }

    public static void next() {
        if (CurrentPlayList.hasList()) {
            play(CurrentPlayList.getNext());
        } else {
            toastNoList();
        }
    }

    public static void previous() {
        if (CurrentPlayList.hasList()) {
            play(CurrentPlayList.getPrevious());
        } else {
            toastNoList();
        }
    }

    public static void toastNoList() {
        CurrentPlay.instance.data = null;
        StandardUtils.toast(R.string.no_play_list);
        notifyCurrentPlayMusicChange();
    }

    public interface OnCurrentPlayMusicChangeListener {
        void onCurrentPlayMusicChange();
    }

    private static ArrayList<OnCurrentPlayMusicChangeListener> listeners = new ArrayList<>(0);

    public static void addToCurrentPlayMusicListeners(OnCurrentPlayMusicChangeListener listener) {
        listeners.add(listener);
    }

    public static void removeFromCurrentPlayMusicListeners(OnCurrentPlayMusicChangeListener listener) {
        listeners.remove(listener);
    }

    static void notifyCurrentPlayMusicChange() {
        for (OnCurrentPlayMusicChangeListener l: listeners) {
            try {
                l.onCurrentPlayMusicChange();
            } catch (Exception e) {
                StandardUtils.printStack(e);
                listeners.remove(l);
            }
        }
    }

    static class Listener implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (CurrentPlayList.instance.playList == null || CurrentPlayList.instance.playList.isEmpty()) {
                return;
            }
            if (CurrentPlayList.instance.mode == CurrentPlayList.PlayMode.ORDER_LIST || CurrentPlayList.instance.mode == CurrentPlayList.PlayMode.RANDOM) {
                next();
            } else if (CurrentPlayList.instance.mode == CurrentPlayList.PlayMode.LOOP_ONE_SONG) {
                MusicPlayServiceConnection.getMediaPlayer().reset();
                MusicPlayServiceConnection.getMediaPlayer().setOnPreparedListener(listener);
                MusicPlayServiceConnection.getMediaPlayer().prepareAsync();
            }
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            notifyCurrentPlayMusicChange();
        }

    }

}

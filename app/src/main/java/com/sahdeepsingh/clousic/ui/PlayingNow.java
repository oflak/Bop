package com.sahdeepsingh.clousic.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.sahdeepsingh.clousic.R;
import com.sahdeepsingh.clousic.SongData.AdapterSong;
import com.sahdeepsingh.clousic.controls.MusicController;
import com.sahdeepsingh.clousic.playerMain.Main;

public class PlayingNow extends ActivityMaster implements MediaController.MediaPlayerControl,AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    /**
     * List that will display all the songs.
     */
    private ListView songListView;

    private boolean paused = false;
    private boolean playbackPaused = false;

    private MusicController musicController;

    /**
     * Thing that maps songs to items on the ListView.
     *
     * We're keeping track of it so we can refresh the ListView if the user
     * wishes to change it's order.
     *
     * Check out the leftmost menu and it's options.
     */
    private AdapterSong songAdapter;

    /**
     * Little menu that will show when the user
     * clicks the ActionBar.
     * It serves to sort the current song list.
     */
    private PopupMenu popup;

    /**
     * Gets called when the Activity is getting initialized.
     */


    private Toolbar toolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_now);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        songListView = (ListView) findViewById(R.id.list_nowplaying);

        // We'll play this pre-defined list.
        // By default we play the first track, although an
        // extra can change this. Look below.
        if(!Main.nowPlayingList.isEmpty())
            Main.musicService.setList(Main.nowPlayingList);
        Main.musicService.setSong(0);

        // Connects the song list to an adapter
        // (thing that creates several Layouts from the song list)
        songAdapter = new AdapterSong(this, Main.nowPlayingList);
        songListView.setAdapter(songAdapter);

        // Looking for optional extras
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {

            // There's the other optional extra - sorting rule
            if (bundle.containsKey("sort"))
                Main.musicService.sortBy((String) bundle.get("sort"));

            // If we received an extra with the song position
            // inside the now playing list, start playing it
            if (bundle.containsKey("song")) {
                int songToPlayIndex = bundle.getInt("song");

                // Prepare the music service to play the song.
                // `setSong` does limit-checking
                Main.musicService.setSong(songToPlayIndex);
            }

            Main.musicService.playSong();

        }

        // Scroll the list view to the current song.
        songListView.setSelection(Main.musicService.currentSongPosition);

        // We'll get warned when the user clicks on an item
        // and when he long selects an item.
        songListView.setOnItemClickListener(this);
        songListView.setOnItemLongClickListener(this);

        setMusicController();

        if (playbackPaused) {
            setMusicController();
            playbackPaused = false;
        }

        // While we're playing music, add an item to the
        // Main Menu that returns here.
        MainScreen.addNowPlayingItem(this);


    }

    /**
     * Shows a Dialog asking the user for a new Playlist name,
     * creating it if so possible.
     */
    private void newPlaylist() {

        // The input box where user will type new name
        final EditText input = new EditText(PlayingNow.this);

        // Labels
        String dialogTitle  = PlayingNow.this.getString(R.string.menu_now_playing_dialog_create_playlist_title);
        String dialogText   = PlayingNow.this.getString(R.string.menu_now_playing_dialog_create_playlist_subtitle);
        String buttonOK     = PlayingNow.this.getString(R.string.menu_now_playing_dialog_create_playlist_button_ok);
        String buttonCancel = PlayingNow.this.getString(R.string.menu_now_playing_dialog_create_playlist_button_cancel);

        // Creating the dialog box that asks the user,
        // with the question and options.
        new AlertDialog.Builder(PlayingNow.this)
                .setTitle(dialogTitle)
                .setMessage(dialogText)
                .setView(input)

                // Creates the OK button, attaching the action to create the Playlist
                .setPositiveButton(buttonOK, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        String playlistName = input.getText().toString();

                        // TODO: Must somehow update the Playlist Activity if it's
                        //       on the background!
                        //       The ListView only updates when Playlist Menu gets
                        //       created from scratch.
                        Main.songs.newPlaylist(PlayingNow.this, "external", playlistName, Main.nowPlayingList);

                        String createPlaylistText = PlayingNow.this.getString(R.string.menu_now_playing_dialog_create_playlist_success, playlistName);

                        // Congratulating the user with the
                        // new Playlist name
                        Toast.makeText(PlayingNow.this,
                                createPlaylistText,
                                Toast.LENGTH_SHORT).show();

                    }

                    // Creates the CANCEL button, that
                    // doesn't do nothing
                    // (since a Playlist is only created
                    // when pressing OK).
                })
                .setNegativeButton(buttonCancel,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing, yay!
                            }

                            // Lol, this is where we actually call the Dialog.
                            // Note for newcomers: The code continues to execute.
                            // This is an asynchronous task.
                        }).show();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN)
            if (keyCode == KeyEvent.KEYCODE_MENU)
                musicController.show();

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if(Main.musicService.isPlaying());
        {

        }
    }

    /**
     * Another Activity is taking focus. (either from user going to another
     * Activity or home)
     */
    @Override
    protected void onPause() {
        super.onPause();

        paused = true;
        playbackPaused = true;
    }

    /**
     * Activity has become visible.
     *
     * @see onPause()
     */
    @Override
    protected void onResume() {
        super.onResume();


        if (paused) {
            // Ensure that the controller
            // is shown when the user returns to the app
            setMusicController();
            paused = false;
        }

        // Scroll the list view to the current song.
        if (Main.settings.get("scroll_on_focus", true))
            songListView.setSelection(Main.musicService.currentSongPosition);
    }

    /**
     * Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        musicController.hide();

        super.onStop();
    }

    /**
     * (Re)Starts the musicController.
     */
    private void setMusicController() {
        musicController = new MusicController(PlayingNow.this);

        // What will happen when the user presses the
        // next/previous buttons?
        musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calling method defined on ActivityNowPlaying
                playNext();
            }
        }, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Calling method defined on ActivityNowPlaying
                playPrevious();
            }
        });

        // Binding to our media player
        musicController.setMediaPlayer(this);
        musicController
                .setAnchorView(findViewById(R.id.activity_now_playing_song_list));
        musicController.setEnabled(true);
    }

    @Override
    public void start() {
        Main.musicService.unpausePlayer();
    }

    /**
     * Callback to when the user pressed the `pause` button.
     */
    @Override
    public void pause() {
        Main.musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (Main.musicService != null && Main.musicService.musicBound
                && Main.musicService.isPlaying())
            return Main.musicService.getDuration();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (Main.musicService != null && Main.musicService.musicBound
                && Main.musicService.isPlaying())
            return Main.musicService.getPosition();
        else
            return 0;
    }

    @Override
    public void seekTo(int position) {
        Main.musicService.seekTo(position);
    }

    @Override
    public boolean isPlaying() {
        if (Main.musicService != null && Main.musicService.musicBound)
            return Main.musicService.isPlaying();

        return false;
    }

    @Override
    public int getBufferPercentage() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        // TODO Auto-generated method stub
        return 0;
    }

    // Back to the normal methods

    /**
     * Jumps to the next song and starts playing it right now.
     */
    public void playNext() {
        Main.musicService.next(true);
        Main.musicService.playSong();


        // To prevent the MusicPlayer from behaving
        // unexpectedly when we pause the song playback.
        if (playbackPaused) {
            setMusicController();
            playbackPaused = false;
        }

        musicController.show();
    }

    /**
     * Jumps to the previous song and starts playing it right now.
     */
    public void playPrevious() {
        Main.musicService.previous(true);
        Main.musicService.playSong();

        // To prevent the MusicPlayer from behaving
        // unexpectedly when we pause the song playback.
        if (playbackPaused) {
            setMusicController();
            playbackPaused = false;
        }

        musicController.show();
    }

    /**
     * When the user selects a music inside the "Now Playing List", we'll start
     * playing it right away.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // Prepare the music service to play the song.
        Main.musicService.setSong(position);

        // Scroll the list view to the current song.
        songListView.setSelection(position);

        Main.musicService.playSong();

        if (playbackPaused) {
            setMusicController();
            playbackPaused = false;
        }
        onResume();
    }

    /**
     * When the user long clicks a music inside the "Now Playing List".
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {

        Toast.makeText(this, Main.musicService.getSong(position).getGenre(),
                Toast.LENGTH_LONG).show();

        // Just a catch - if we return `false`, when an user
        // long clicks an item, the list will react as if
        // we've long clicked AND clicked.
        //
        // So by returning `false`, it will call both
        // `onItemLongClick` and `onItemClick`!
        return true;
    }
}
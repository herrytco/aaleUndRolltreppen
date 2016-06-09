package at.aau.group1.leiterspiel;

import android.view.View;

/**
 * Created by marku on 07.06.2016.
 */
public class Fullscreen {

    View mDecorView;
    //This should hide system bars

    public void hideSystemUI() {
        //Set IMMERSIVE Flag
        //Set content to appear UNDER system bars
        //No resizeing when sys bar hides/shows


        getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.

    public void showSystemUI(){
        getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    public void setDecorView(View decorView) {
        mDecorView = decorView;
    }

    public View getDecorView() {
        return mDecorView;
    }



    //non-sticky fullscreen


}

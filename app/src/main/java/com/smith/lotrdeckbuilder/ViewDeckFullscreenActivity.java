package com.smith.lotrdeckbuilder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;

import com.smith.lotrdeckbuilder.game.Card;
import com.smith.lotrdeckbuilder.game.Deck;
import com.smith.lotrdeckbuilder.game.Pack;
import com.smith.lotrdeckbuilder.helper.AppManager;
import com.smith.lotrdeckbuilder.helper.ImageDisplayer;
import com.smith.lotrdeckbuilder.helper.Sorter.CardSorterByCardNumber;
import com.smith.lotrdeckbuilder.helper.Sorter.CardSorterByCardType;
import com.smith.lotrdeckbuilder.util.SystemUiHider;

import java.util.ArrayList;
import java.util.Collections;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ViewDeckFullscreenActivity extends ActionBarActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;


    // Arguments
    public static final String EXTRA_DECK_ID = "com.example.lotrdeckbuilder.EXTRA_DECK_ID";
    public static final String EXTRA_SET_NAME = "com.example.lotrdeckbuilder.EXTRA_SET_NAME";
    public static final String EXTRA_CARD_CODE = "com.example.lotrdeckbuilder.EXTRA_CARD_CODE";
    public static final String EXTRA_POSITION = "com.example.lotrdeckbuilder.EXTRA_POSITION";

    // GUI Elements
    private ViewPager mPager;

    private Deck mDeck = null;
    private String mSetName = null;
    private String mCardCode = null;
    private int mPosition = 0;

    private ArrayList<Card> mCards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    // Get the deck / set name
        if (savedInstanceState == null) {
            mDeck = AppManager.getInstance().getDeck(getIntent().getLongExtra(EXTRA_DECK_ID, 0));
            mSetName = getIntent().getStringExtra(EXTRA_SET_NAME);
            mCardCode = getIntent().getStringExtra(EXTRA_CARD_CODE);
            mPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        } else {
            mDeck = AppManager.getInstance().getDeck(savedInstanceState.getLong(EXTRA_DECK_ID, 0));
            mSetName = savedInstanceState.getString(EXTRA_SET_NAME);
            mCardCode = savedInstanceState.getString(EXTRA_CARD_CODE);
            mPosition = savedInstanceState.getInt(EXTRA_POSITION);
        }

	    // set theme to identity's faction colors
	    if (mDeck != null)
	    {
		    setTheme(getResources().getIdentifier("Theme.Lotr_" + mDeck.getIdentity().getSphereCode().replace("-", ""), "style", this.getPackageName()));
	    }
	    else if (mCardCode != null)
	    {
		    setTheme(getResources().getIdentifier("Theme.Lotr_" + AppManager.getInstance().getCard(mCardCode).getSphereCode().replace("-", ""), "style", this.getPackageName()));
	    }

        setContentView(R.layout.activity_fullscreen_view);

        // GUI
        mPager = (ViewPager) findViewById(R.id.pager);

        // Build the image array
        if (mDeck != null) {
            mCards = mDeck.getCards();
            getSupportActionBar().setIcon(mDeck.getIdentity().getFactionImageRes(this));
            Collections.sort(mCards, new CardSorterByCardType());
        } else if (mSetName != null) {
            mCards = AppManager.getInstance().getCardsBySetName(mSetName);
            Collections.sort(mCards, new CardSorterByCardNumber());
        } else if (mCardCode != null) {
            mCards = new ArrayList<Card>();
            mCards.add(AppManager.getInstance().getCard(mCardCode));
        }

        // Quit if deck is empty
        if (mCards.size() == 0) {
            exitIfDeckEmpty();
            return;
        }

        // Change the icon and title
        updateTitle(mCards.get(0));

        // Set the adapter for the view pager
        mPager.setAdapter(new ImageViewPager());
        mPager.setCurrentItem(mPosition);
        mPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                Card card = mCards.get(arg0);
                updateTitle(card);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDeck != null)
            outState.putLong(EXTRA_DECK_ID, mDeck.getRowId());
        outState.putString(EXTRA_SET_NAME, mSetName);
        outState.putString(EXTRA_CARD_CODE, mCardCode);
        outState.putInt(EXTRA_POSITION, mPosition);
    }

    private void exitIfDeckEmpty() {
        if (mCards.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.view_deck);
            builder.setMessage(R.string.deck_is_empty);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
        }
    }

    private void updateTitle(Card card) {
        if (mDeck != null) {
            setTitle("[" + mDeck.getCardCount(card) + "] - " + mDeck.getName());
        } else if (mSetName != null) {
            for (Pack pack : AppManager.getInstance().getAllPacks()) {
                if (pack.getCode().equals(mSetName)) {
                    setTitle(pack.getName());
                    break;
                }
            }
        } else {
            setTitle(card.getTitle());
        }
    }


    private class ImageViewPager extends PagerAdapter {

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (LinearLayout) arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Card card = mCards.get(position);
            LinearLayout v = new LinearLayout(ViewDeckFullscreenActivity.this);
            v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            v.setBackgroundColor(Color.BLACK);
            ImageView imgCard = new ImageView(ViewDeckFullscreenActivity.this);
            imgCard.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            //imgCard.setImageBitmap(card.getImage(ViewDeckFullscreenActivity.this));
            ImageDisplayer.fill(imgCard, card, ViewDeckFullscreenActivity.this);
            imgCard.setVisibility(View.VISIBLE);
            // Close the activity if the image is clicked
            imgCard.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (AppManager.getInstance().getSharedPrefs().getBoolean(SettingsActivity.KEY_PREF_TAP_TO_CLOSE_CARD_PREVIEW, false)) {
                        ViewDeckFullscreenActivity.this.finish();
                    }
                }
            });
            v.addView(imgCard);
            ((ViewPager) container).addView(v, 0);
            return v;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mCards.get(position).getTitle();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }


//
//	@Override
//	protected void onPostCreate(Bundle savedInstanceState) {
//		super.onPostCreate(savedInstanceState);
//
//		// Trigger the initial hide() shortly after the activity has been
//		// created, to briefly hint to the user that UI controls
//		// are available.
//		delayedHide(100);
//	}
//
//	/**
//	 * Touch listener to use for in-layout UI controls to delay hiding the
//	 * system UI. This is to prevent the jarring behavior of controls going away
//	 * while interacting with activity UI.
//	 */
//	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//		@Override
//		public boolean onTouch(View view, MotionEvent motionEvent) {
//			if (AUTO_HIDE) {
//				delayedHide(AUTO_HIDE_DELAY_MILLIS);
//			}
//			return false;
//		}
//	};

//	Handler mHideHandler = new Handler();
//	Runnable mHideRunnable = new Runnable() {
//		@Override
//		public void run() {
//			mSystemUiHider.hide();
//		}
//	};
//
//	/**
//	 * Schedules a call to hide() in [delay] milliseconds, canceling any
//	 * previously scheduled calls.
//	 */
//	private void delayedHide(int delayMillis) {
//		mHideHandler.removeCallbacks(mHideRunnable);
//		mHideHandler.postDelayed(mHideRunnable, delayMillis);
//	}
}

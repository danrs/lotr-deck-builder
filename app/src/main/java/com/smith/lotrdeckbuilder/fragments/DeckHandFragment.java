package com.smith.lotrdeckbuilder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.smith.lotrdeckbuilder.R;
import com.smith.lotrdeckbuilder.ViewDeckFullscreenActivity;
import com.smith.lotrdeckbuilder.adapters.HandCardsListAdapter;
import com.smith.lotrdeckbuilder.game.Card;
import com.smith.lotrdeckbuilder.game.Deck;
import com.smith.lotrdeckbuilder.helper.AppManager;

import java.util.ArrayList;
import java.util.Random;

public class DeckHandFragment extends Fragment {

    // GUI
    private ListView lstCards;
    private Button btnNewHand;
    private Button btnDraw;

    // Arguments
    public static final String ARGUMENT_DECK_ID = "com.example.lotrdeckbuilder.ARGUMENT_DECK_ID";

    // Cards
    private Card[] mCards;
    private Deck mDeck;
    private HandCardsListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View theView = inflater.inflate(R.layout.fragment_deck_hand, container, false);

        // Arguments
        mDeck = AppManager.getInstance().getDeck(getArguments().getLong(ARGUMENT_DECK_ID));
        mCards = new Card[mDeck.getDeckSize()];
        int num = 0;
        for (int i = 0; i < mDeck.getCards().size(); i++) {
            Card card = mDeck.getCards().get(i);
            for (int j = 0; j < mDeck.getCardCount(card); j++) {
                mCards[num++] = card;
            }
        }

        // GUI
        lstCards = (ListView) theView.findViewById(R.id.lstCards);
        btnNewHand = (Button) theView.findViewById(R.id.btnNewHand);
        btnDraw = (Button) theView.findViewById(R.id.btnDraw);

        // Disable the btnDraw if necessary
        btnDraw.setEnabled(mCards.length > 0);

        // List
        mAdapter = new HandCardsListAdapter(getActivity(), new ArrayList<Card>());
        lstCards.setAdapter(mAdapter);

        // Handle the clicks
        btnNewHand.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Clear and generate 5 new cards (or 9 for Andromeda identity
                int handSize = 5;
                if (mDeck.getIdentity().getCode().equals(Card.SpecialCards.CARD_ANDROMEDA))
                    handSize = 9;
                mAdapter.clear();
                // Shuffle
                shuffleArray(mCards);
                // Display the first hand
                for (int i = 0; i < Math.min(handSize, mCards.length); i++) {
                    mAdapter.add(mCards[i]);
                }
                // Disable the btnDraw if necessary
                btnDraw.setEnabled(mAdapter.getCount() < mCards.length);
            }
        });
        btnDraw.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Add a new card
                mAdapter.add(mCards[mAdapter.getCount()]);
                // Disable the btnDraw if necessary
                btnDraw.setEnabled(mAdapter.getCount() < mCards.length);
            }
        });
        lstCards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Card card = (Card) adapterView.getAdapter().getItem(i);
                Intent intent = new Intent(getActivity(), ViewDeckFullscreenActivity.class);
                intent.putExtra(ViewDeckFullscreenActivity.EXTRA_CARD_CODE, card.getCode());
                startActivity(intent);
            }
        });

        return theView;
    }

    // Implementing Fisher-Yates shuffle
    private void shuffleArray(Card[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Card a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

}

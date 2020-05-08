package com.brizztv.mcube;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View.OnClickListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.brizztv.mcube.R;

public class TabFragment extends SherlockFragment {
	private static final int EXPENSE_LIST_STATE = 0x1;
	private static final int REMINDER_LIST_STATE = 0x2;
	private int mTabState;
	
   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab, container, false);
        
        // Grab the tab buttons from the layout and attach event handlers. The code just uses standard
        // buttons for the tab widgets. These are bad tab widgets, design something better, this is just
        // to keep the code simple.
        Button listViewTab = (Button) view.findViewById(R.id.expenses_tab);
        Button gridViewTab = (Button) view.findViewById(R.id.reminders_tab);
        
        listViewTab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch the tab content to display the list view.
                gotoExpenseListView();
            }
        });
        
        gridViewTab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch the tab content to display the grid view.
                gotoReminderListView();
            }
        });
        
        return view;
    }
   
   public void gotoExpenseListView() {
       // mTabState keeps track of which tab is currently displaying its contents.
       // Perform a check to make sure the list tab content isn't already displaying.
       
       if (mTabState != EXPENSE_LIST_STATE) {
           // Update the mTabState 
           mTabState = EXPENSE_LIST_STATE;
           
           // Fragments have access to their parent Activity's FragmentManager. You can
           // obtain the FragmentManager like this.
           FragmentManager fm = getFragmentManager();
           
           if (fm != null) {
               // Perform the FragmentTransaction to load in the list tab content.
               // Using FragmentTransaction#replace will destroy any Fragments
               // currently inside R.id.fragment_content and add the new Fragment
               // in its place.
               FragmentTransaction ft = fm.beginTransaction();
//               ft.replace(R.id.fragment_content, new ExpenseListFragment());
               ft.commit();
           }
       }
   }
   
   public void gotoReminderListView() {
       // See gotoListView(). This method does the same thing except it loads
       // the grid tab.
       
       if (mTabState != REMINDER_LIST_STATE) {
           mTabState = REMINDER_LIST_STATE;
           
           FragmentManager fm = getFragmentManager();
           
           if (fm != null) {
               FragmentTransaction ft = fm.beginTransaction();
//               ft.replace(R.id.fragment_content, new ReminderListFragment());
               ft.commit();
           }
       }
   }
}

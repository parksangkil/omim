package com.mapswithme.country;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mapswithme.maps.MWMActivity;
import com.mapswithme.maps.R;
import com.mapswithme.maps.base.MWMListFragment;
import com.mapswithme.maps.base.OnBackPressListener;
import com.mapswithme.util.UiUtils;

public class DownloadFragment extends MWMListFragment implements View.OnClickListener, ActiveCountryTree.ActiveCountryListener, OnBackPressListener
{
  private ExtendedDownloadAdapterWrapper mExtendedAdapter;
  private DownloadedAdapter mDownloadedAdapter;
  private TextView mTvUpdateAll;
  private int mMode = MODE_DISABLED;
  private int mListenerSlotId;

  private static final int MODE_DISABLED = -1;
  private static final int MODE_NONE = 0;
  private static final int MODE_UPDATE_ALL = 1;
  private static final int MODE_CANCEL_ALL = 2;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.fragment_downloader, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);
    initToolbar();
    if (getArguments() != null && getArguments().getBoolean(DownloadActivity.EXTRA_OPEN_DOWNLOADED_LIST, false))
      openDownloadedList();
    else
    {
      mExtendedAdapter = getExtendedAdapter();
      setListAdapter(mExtendedAdapter);
      mMode = MODE_DISABLED;
      mListenerSlotId = ActiveCountryTree.addListener(this);
    }
  }

  private void initToolbar()
  {
    UiUtils.showHomeUpButton(getToolbar());
    getToolbar().setTitle(getString(R.string.maps));
    getToolbar().setNavigationOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        onBackPressed();
      }
    });
    mTvUpdateAll = (TextView) getToolbar().findViewById(R.id.tv__update_all);
    mTvUpdateAll.setOnClickListener(this);
    mTvUpdateAll.setVisibility(View.GONE);
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();

    ActiveCountryTree.removeListener(mListenerSlotId);
  }


  private BaseDownloadAdapter getDownloadAdapter()
  {
    return (BaseDownloadAdapter) getListView().getAdapter();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    getDownloadAdapter().onResume(getListView());
  }

  @Override
  public void onPause()
  {
    super.onPause();
    getDownloadAdapter().onPause();
  }

  @Override
  public boolean onBackPressed()
  {
    if (getDownloadAdapter().onBackPressed())
    {
      // scroll list view to the top
      setSelection(0);
      return true;
    }
    else if (getListAdapter() instanceof DownloadedAdapter)
    {
      mMode = MODE_DISABLED;
      mDownloadedAdapter.onPause();
      mExtendedAdapter = getExtendedAdapter();
      mExtendedAdapter.onResume(getListView());
      setListAdapter(mExtendedAdapter);
      updateToolbar();
      return true;
    }
    else
    {
      // FIXME
      // Always show map as parent
      startActivity(new Intent(getActivity(), MWMActivity.class));
    }
    return false;
  }

  private void updateToolbar()
  {
    if (mTvUpdateAll == null)
      return;
    if (mMode == MODE_DISABLED)
    {
      mTvUpdateAll.setVisibility(View.GONE);
      return;
    }

    updateMode();
    switch (mMode)
    {
    case MODE_CANCEL_ALL:
      mTvUpdateAll.setText(getString(R.string.downloader_cancel_all));
      mTvUpdateAll.setVisibility(View.VISIBLE);
      break;
    case MODE_UPDATE_ALL:
      mTvUpdateAll.setText(getString(R.string.downloader_update_all));
      mTvUpdateAll.setVisibility(View.VISIBLE);
      break;
    case MODE_NONE:
      mTvUpdateAll.setVisibility(View.GONE);
      break;
    }
  }

  private void updateMode()
  {
    if (ActiveCountryTree.isDownloadingActive())
      mMode = MODE_CANCEL_ALL;
    else if (ActiveCountryTree.getOutOfDateCount() > 0)
      mMode = MODE_UPDATE_ALL;
    else
      mMode = MODE_NONE;
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id)
  {
    if (getListAdapter().getItemViewType(position) == ExtendedDownloadAdapterWrapper.TYPE_EXTENDED)
      openDownloadedList();
  }

  private void openDownloadedList()
  {
    setListAdapter(getDownloadedAdapter());
    mMode = MODE_NONE;
    updateToolbar();
    if (mExtendedAdapter != null)
      mExtendedAdapter.onPause();
    mDownloadedAdapter.onResume(getListView());
  }

  private ListAdapter getDownloadedAdapter()
  {
    if (mDownloadedAdapter == null)
    {
      mDownloadedAdapter = new DownloadedAdapter(getActivity());
      mDownloadedAdapter.registerDataSetObserver(new DataSetObserver()
      {
        @Override
        public void onChanged()
        {
          updateToolbar();
        }
      });
    }

    return mDownloadedAdapter;
  }

  private ExtendedDownloadAdapterWrapper getExtendedAdapter()
  {
    if (mExtendedAdapter == null)
    {
      mExtendedAdapter = new ExtendedDownloadAdapterWrapper(getActivity(), new DownloadAdapter(getActivity()));
      mExtendedAdapter.registerDataSetObserver(new DataSetObserver()
      {
        @Override
        public void onChanged()
        {
          updateToolbar();
        }
      });
    }

    return mExtendedAdapter;
  }

  @Override
  public void onClick(View v)
  {
    switch (v.getId())
    {
    case R.id.tv__update_all:
      if (mMode == MODE_UPDATE_ALL)
        ActiveCountryTree.updateAll();
      else
        ActiveCountryTree.cancelAll();
      updateToolbar();
      break;
    }
  }

  @Override
  public void onCountryProgressChanged(int group, int position, long[] sizes) { }

  @Override
  public void onCountryStatusChanged(int group, int position, int oldStatus, int newStatus)
  {
    updateToolbar();
  }

  @Override
  public void onCountryGroupChanged(int oldGroup, int oldPosition, int newGroup, int newPosition) { }

  @Override
  public void onCountryOptionsChanged(int group, int position, int newOptions, int requestOptions)
  {
    updateToolbar();
  }
}
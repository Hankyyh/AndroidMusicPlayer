package com.wwj.sb.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.method.DigitsKeyListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wwj.sb.adapter.MenuAdapter;
import com.wwj.sb.adapter.MusicListAdapter;
import com.wwj.sb.custom.Menu;
import com.wwj.sb.domain.AppConstant;
import com.wwj.sb.domain.Contsant;
import com.wwj.sb.domain.Mp3Info;
import com.wwj.sb.service.PlayerService;
import com.wwj.sb.utils.ConstantUtil;
import com.wwj.sb.utils.CustomDialog;
import com.wwj.sb.utils.MediaUtil;
import com.wwj.sb.utils.Settings;

/**
 * 2013/5/7 �������ֲ�����
 * 
 * @author wwj
 * 
 */
public class HomeActivity extends BaseActivity {
	private ListView mMusiclist; // �����б�
	private List<Mp3Info> mp3Infos = null;
	// private SimpleAdapter mAdapter; // ��������
	MusicListAdapter listAdapter; // ��Ϊ�Զ����б�������
	private Button previousBtn; // ��һ��
	private Button repeatBtn; // �ظ�������ѭ����ȫ��ѭ����
	private Button playBtn; // ���ţ����š���ͣ��
	private Button shuffleBtn; // �������
	private Button nextBtn; // ��һ��
	private TextView musicTitle;// ��������
	private TextView musicDuration; // ����ʱ��
	private Button musicPlaying; // ����ר��
	private ImageView musicAlbum; // ר������

	private int repeatState; // ѭ����ʶ
	private final int isCurrentRepeat = 1; // ����ѭ��
	private final int isAllRepeat = 2; // ȫ��ѭ��
	private final int isNoneRepeat = 3; // ���ظ�����
	private boolean isFirstTime = true;
	private boolean isPlaying; // ���ڲ���
	private boolean isPause; // ��ͣ
	private boolean isNoneShuffle = true; // ˳�򲥷�
	private boolean isShuffle = false; // �������

	private int listPosition = 0; // ��ʶ�б�λ��
	private HomeReceiver homeReceiver; // �Զ���Ĺ㲥������
	// һϵ�ж���
	public static final String UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION"; // ���¶���
	public static final String CTL_ACTION = "com.wwj.action.CTL_ACTION"; // ���ƶ���
	public static final String MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT"; // ��ǰ���ָı䶯��
	public static final String MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION"; // ����ʱ���ı䶯��
	public static final String REPEAT_ACTION = "com.wwj.action.REPEAT_ACTION"; // �����ظ��ı䶯��
	public static final String SHUFFLE_ACTION = "com.wwj.action.SHUFFLE_ACTION"; // ����������Ŷ���

	private int currentTime; // ��ǰʱ��
	private int duration; // ʱ��

	private Menu xmenu; // �Զ���˵�
	private Toast toast;

	private TextView timers;// ��ʾ����ʱ������
	private Timers timer;// ����ʱ�ڲ�����
	private int c;// cȷ��һ����ʶ

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_activity_layout);

		mMusiclist = (ListView) findViewById(R.id.music_list);
		mMusiclist.setOnItemClickListener(new MusicListItemClickListener());
		mMusiclist
				.setOnCreateContextMenuListener(new MusicListItemContextMenuListener());
		mp3Infos = MediaUtil.getMp3Infos(HomeActivity.this); // ��ȡ�������󼯺�
		// setListAdpter(MediaUtil.getMusicMaps(mp3Infos)); //��ʾ�����б�
		listAdapter = new MusicListAdapter(this, mp3Infos);
		mMusiclist.setAdapter(listAdapter);
		findViewById(); // �ҵ������ϵ�ÿһ���ؼ�
		setViewOnclickListener(); // ΪһЩ�ؼ����ü�����
		repeatState = isNoneRepeat; // ��ʼ״̬Ϊ���ظ�����״̬

		homeReceiver = new HomeReceiver();
		// ����IntentFilter
		IntentFilter filter = new IntentFilter();
		// ָ��BroadcastReceiver������Action
		filter.addAction(UPDATE_ACTION);
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		filter.addAction(REPEAT_ACTION);
		filter.addAction(SHUFFLE_ACTION);
		// ע��BroadcastReceiver
		registerReceiver(homeReceiver, filter);

		LoadMenu();

	}

	private void LoadMenu() {
		xmenu = new Menu(this);
		List<int[]> data1 = new ArrayList<int[]>();
		data1.add(new int[] { R.drawable.btn_menu_skin, R.string.skin_settings });
		data1.add(new int[] { R.drawable.btn_menu_exit, R.string.menu_exit_txt });

		xmenu.addItem("����", data1, new MenuAdapter.ItemListener() {

			@Override
			public void onClickListener(int position, View view) {
				xmenu.cancel();
				if (position == 0) {
					Intent intent = new Intent(HomeActivity.this,
							SkinSettingActivity.class);
					startActivityForResult(intent, 2);
				} else if (position == 1) {
					exit();
				}
			}
		});
		List<int[]> data2 = new ArrayList<int[]>();
		data2.add(new int[] { R.drawable.btn_menu_setting,
				R.string.menu_settings });
		data2.add(new int[] { R.drawable.btn_menu_sleep, R.string.menu_time_txt});
		Settings setting = new Settings(this, false);
		String brightness = setting.getValue(Settings.KEY_BRIGHTNESS);
		if (brightness != null && brightness.equals("0")) { // ҹ��ģʽ
			data2.add(new int[] { R.drawable.btn_menu_brightness,
					R.string.brightness_title });
		} else {
			data2.add(new int[] { R.drawable.btn_menu_darkness,
					R.string.darkness_title });
		}
		xmenu.addItem("����", data2, new MenuAdapter.ItemListener() {

			@Override
			public void onClickListener(int position, View view) {
				xmenu.cancel();
				if (position == 0) {

				} else if (position == 1) {
					Sleep();
				} else if (position == 2) {
					setBrightness(view);
				}
			}

		});
		List<int[]> data3 = new ArrayList<int[]>();
		data3.add(new int[] { R.drawable.btn_menu_about, R.string.about_title });
		xmenu.addItem("����", data3, new MenuAdapter.ItemListener() {
			@Override
			public void onClickListener(int position, View view) {
				xmenu.cancel();
				Intent intent = new Intent(HomeActivity.this,AboutActivity.class);
				startActivity(intent);
			}
		});
		xmenu.create();		//�����˵�
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		menu.add("menu");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, android.view.Menu menu) {
		// �˵���������ʾ������1�Ǹò����ܵ�ID���ڶ���λ�ã��������ĸ���XY����
		xmenu.showAtLocation(findViewById(R.id.homeRLLayout), Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 0, 0);
		// �������true�Ļ��ͻ���ʾϵͳ�Դ��Ĳ˵�����֮����false�Ļ�������ʾ�Լ�д��
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && requestCode == 1) {
			Settings setting = new Settings(this, false);
			this.getWindow().setBackgroundDrawableResource(
					setting.getCurrentSkinResId());
		}
	}

	/**
	 * �˳�����
	 */
	private void exit() {
		Intent intent = new Intent(HomeActivity.this, PlayerService.class);
		stopService(intent);
		finish();
	}

	/**
	 * ���߷���
	 */
   private void Sleep(){
	   final EditText edtext = new EditText(this);
	   edtext.setText("5");//���ó�ʼֵ
		edtext.setKeyListener(new DigitsKeyListener(false, true));
		edtext.setGravity(Gravity.CENTER_HORIZONTAL);//���ð���λ��
		edtext.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//��������
		edtext.setTextColor(Color.BLUE);//������ɫ
		edtext.setSelection(edtext.length());//����ѡ��λ��
		edtext.selectAll();//ȫ��ѡ��
	    new CustomDialog.Builder(HomeActivity.this).setTitle("������ʱ��").setView(edtext).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				dialog.cancel();
				/**�������С��2���ߵ���0���֪�û�**/
				if (edtext.length() <= 2 && edtext.length() != 0) {
					if (".".equals(edtext.getText().toString())) {
						toast = Contsant.showMessage(toast,HomeActivity.this, "�������������������λ����.");
					} else {
						final String time = edtext.getText().toString();
						long Money = Integer.parseInt(time);
						long cX = Money * 60000;
						timer= new Timers(cX, 1000);
					    timer.start();//����ʱ��ʼ
						toast = Contsant.showMessage(toast,HomeActivity.this, "����ģʽ����!��" + String.valueOf(time)+ "���Ӻ�رճ���!");
						timers.setVisibility(View.INVISIBLE);
						timers.setVisibility(View.VISIBLE);
						timers.setText(String.valueOf(time));
					}

				} else {
					Toast.makeText(HomeActivity.this, "�����뼸����",Toast.LENGTH_SHORT).show();
				}
				
			}
		}).setNegativeButton(R.string.cancel, null).show();
		
}

	private class Timers extends CountDownTimer {

		public Timers(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			timers.setText("" + millisUntilFinished / 1000 / 60 + ":"
					+ millisUntilFinished / 1000 % 60);
			// �������������9˵������2λ���ˣ�����ֱ�����롣����С�ڵ���9����1λ��������ǰ���һ��0
			String abc = (millisUntilFinished / 1000 / 60) > 9 ? (millisUntilFinished / 1000 / 60)
					+ ""
					: "0" + (millisUntilFinished / 1000 / 60);
			String b = (millisUntilFinished / 1000 % 60) > 9 ? (millisUntilFinished / 1000 % 60)
					+ ""
					: "0" + (millisUntilFinished / 1000 % 60);
			timers.setText(abc + ":" + b);
			timers.setVisibility(View.GONE);
		}

		@Override
		public void onFinish() {
			if (c == 0) {
				exit();
				finish();
				onDestroy();
			} else {
				finish();
				onDestroy();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}
	}

	/**
	 * �ӽ����ϸ���id��ȡ��ť
	 */
	private void findViewById() {
		previousBtn = (Button) findViewById(R.id.previous_music);
		repeatBtn = (Button) findViewById(R.id.repeat_music);
		playBtn = (Button) findViewById(R.id.play_music);
		shuffleBtn = (Button) findViewById(R.id.shuffle_music);
		nextBtn = (Button) findViewById(R.id.next_music);
		musicTitle = (TextView) findViewById(R.id.music_title);
		musicDuration = (TextView) findViewById(R.id.music_duration);
		musicPlaying = (Button) findViewById(R.id.playing);
		musicAlbum = (ImageView) findViewById(R.id.music_album);
	}

	/**
	 * ��ÿһ����ť���ü�����
	 */
	private void setViewOnclickListener() {
		ViewOnClickListener viewOnClickListener = new ViewOnClickListener();
		previousBtn.setOnClickListener(viewOnClickListener);
		repeatBtn.setOnClickListener(viewOnClickListener);
		playBtn.setOnClickListener(viewOnClickListener);
		shuffleBtn.setOnClickListener(viewOnClickListener);
		nextBtn.setOnClickListener(viewOnClickListener);
		musicPlaying.setOnClickListener(viewOnClickListener);
	}

	/**
	 * �ؼ��ļ�����
	 * 
	 * @author Administrator
	 * 
	 */
	private class ViewOnClickListener implements OnClickListener {
		Intent intent = new Intent();

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.previous_music: // ��һ��
				playBtn.setBackgroundResource(R.drawable.play_selector);
				isFirstTime = false;
				isPlaying = true;
				isPause = false;
				previous();
				break;
			case R.id.repeat_music: // �ظ�����
				if (repeatState == isNoneRepeat) {
					repeat_one();
					shuffleBtn.setClickable(false);
					repeatState = isCurrentRepeat;
				} else if (repeatState == isCurrentRepeat) {
					repeat_all();
					shuffleBtn.setClickable(false);
					repeatState = isAllRepeat;
				} else if (repeatState == isAllRepeat) {
					repeat_none();
					shuffleBtn.setClickable(true);
					repeatState = isNoneRepeat;
				}
				switch (repeatState) {
				case isCurrentRepeat: // ����ѭ��
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_current_selector);
					Toast.makeText(HomeActivity.this, R.string.repeat_current,
							Toast.LENGTH_SHORT).show();
					break;
				case isAllRepeat: // ȫ��ѭ��
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_all_selector);
					Toast.makeText(HomeActivity.this, R.string.repeat_all,
							Toast.LENGTH_SHORT).show();
					break;
				case isNoneRepeat: // ���ظ�
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_none_selector);
					Toast.makeText(HomeActivity.this, R.string.repeat_none,
							Toast.LENGTH_SHORT).show();
					break;
				}

				break;
			case R.id.play_music: // ��������
				if (isFirstTime) {
					play();
					isFirstTime = false;
					isPlaying = true;
					isPause = false;
				} else {
					if (isPlaying) {
						playBtn.setBackgroundResource(R.drawable.pause_selector);
						intent.setAction("com.wwj.media.MUSIC_SERVICE");
						intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
						startService(intent);
						isPlaying = false;
						isPause = true;

					} else if (isPause) {
						playBtn.setBackgroundResource(R.drawable.play_selector);
						intent.setAction("com.wwj.media.MUSIC_SERVICE");
						intent.putExtra("MSG",
								AppConstant.PlayerMsg.CONTINUE_MSG);
						startService(intent);
						isPause = false;
						isPlaying = true;
					}
				}
				break;
			case R.id.shuffle_music: // �������
				if (isNoneShuffle) {
					shuffleBtn
							.setBackgroundResource(R.drawable.shuffle_selector);
					Toast.makeText(HomeActivity.this, R.string.shuffle,
							Toast.LENGTH_SHORT).show();
					isNoneShuffle = false;
					isShuffle = true;
					shuffleMusic();
					repeatBtn.setClickable(false);
				} else if (isShuffle) {
					shuffleBtn
							.setBackgroundResource(R.drawable.shuffle_none_selector);
					Toast.makeText(HomeActivity.this, R.string.shuffle_none,
							Toast.LENGTH_SHORT).show();
					isShuffle = false;
					isNoneShuffle = true;
					repeatBtn.setClickable(true);
				}
				break;
			case R.id.next_music: // ��һ��
				playBtn.setBackgroundResource(R.drawable.play_selector);
				isFirstTime = false;
				isPlaying = true;
				isPause = false;
				next();
				break;
			case R.id.playing: // ���ڲ���
				Mp3Info mp3Info = mp3Infos.get(listPosition);
				Intent intent = new Intent(HomeActivity.this,
						PlayerActivity.class);
				intent.putExtra("title", mp3Info.getTitle());
				intent.putExtra("url", mp3Info.getUrl());
				intent.putExtra("artist", mp3Info.getArtist());
				intent.putExtra("listPosition", listPosition);
				intent.putExtra("currentTime", currentTime);
				intent.putExtra("duration", duration);
				intent.putExtra("MSG", AppConstant.PlayerMsg.PLAYING_MSG);
				startActivity(intent);
				break;
			}
		}
	}

	/**
	 * �б���������
	 * 
	 * @author wwj
	 * 
	 */
	private class MusicListItemClickListener implements OnItemClickListener {
		/**
		 * ����б�������
		 */
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			listPosition = position; // ��ȡ�б�����λ��
			playMusic(listPosition); // ��������
		}

	}

	/**
	 * �����Ĳ˵���ʾ������
	 * 
	 * @author Administrator
	 * 
	 */
	public class MusicListItemContextMenuListener implements
			OnCreateContextMenuListener {

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
			vibrator.vibrate(50); // ������
			musicListItemDialog(); // �����󵯳��ĶԻ���
			final AdapterView.AdapterContextMenuInfo menuInfo2 = (AdapterView.AdapterContextMenuInfo) menuInfo;
			listPosition = menuInfo2.position; // ����б��λ��
		}

	}

	/**
	 * ����б�
	 * 
	 * @param mp3Infos
	 */
	// public void setListAdpter(List<HashMap<String, String>> mp3list) {
	// mAdapter = new SimpleAdapter(this, mp3list,
	// R.layout.music_list_item_layout, new String[] {"title",
	// "Artist", "duration" }, new int[] {R.id.music_title,
	// R.id.music_Artist, R.id.music_duration });
	// // mMusiclist.setAdapter(mAdapter);
	//
	// }

	/**
	 * ��һ�׸���
	 */
	public void next() {
		listPosition = listPosition + 1;
		if (listPosition <= mp3Infos.size() - 1) {
			Mp3Info mp3Info = mp3Infos.get(listPosition);
			musicTitle.setText(mp3Info.getTitle());
			Intent intent = new Intent();
			intent.setAction("com.wwj.media.MUSIC_SERVICE");
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("MSG", AppConstant.PlayerMsg.NEXT_MSG);
			startService(intent);
		} else {
			listPosition = mp3Infos.size() - 1;
			Toast.makeText(HomeActivity.this, "û����һ����", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * ��һ�׸���
	 */
	public void previous() {
		listPosition = listPosition - 1;
		if (listPosition >= 0) {
			Mp3Info mp3Info = mp3Infos.get(listPosition);
			musicTitle.setText(mp3Info.getTitle());
			Intent intent = new Intent();
			intent.setAction("com.wwj.media.MUSIC_SERVICE");
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("MSG", AppConstant.PlayerMsg.PRIVIOUS_MSG);
			startService(intent);
		} else {
			listPosition = 0;
			Toast.makeText(HomeActivity.this, "û����һ����", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * ��������
	 */
	public void play() {
		playBtn.setBackgroundResource(R.drawable.play_selector);
		Mp3Info mp3Info = mp3Infos.get(listPosition);
		musicTitle.setText(mp3Info.getTitle());
		Intent intent = new Intent();
		intent.setAction("com.wwj.media.MUSIC_SERVICE");
		intent.putExtra("listPosition", 0);
		intent.putExtra("url", mp3Info.getUrl());
		intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
		startService(intent);
	}

	/**
	 * ����ѭ��
	 */
	public void repeat_one() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 1);
		sendBroadcast(intent);
	}

	/**
	 * ȫ��ѭ��
	 */
	public void repeat_all() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 2);
		sendBroadcast(intent);
	}

	/**
	 * ˳�򲥷�
	 */
	public void repeat_none() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 3);
		sendBroadcast(intent);
	}

	/**
	 * �������
	 */
	public void shuffleMusic() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 4);
		sendBroadcast(intent);
	}

	/**
	 * �Զ���Ի���
	 */
	public void musicListItemDialog() {
		String[] menuItems = new String[] { "��������", "��Ϊ����", "�鿴����" };
		ListView menuList = new ListView(HomeActivity.this);
		menuList.setCacheColorHint(Color.TRANSPARENT);
		menuList.setDividerHeight(1);
		menuList.setAdapter(new ArrayAdapter<String>(HomeActivity.this,
				R.layout.context_dialog_layout, R.id.dialogText, menuItems));
		menuList.setLayoutParams(new LayoutParams(ConstantUtil
				.getScreen(HomeActivity.this)[0] / 2, LayoutParams.WRAP_CONTENT));

		final CustomDialog customDialog = new CustomDialog.Builder(
				HomeActivity.this).setTitle(R.string.operation)
				.setView(menuList).create();
		customDialog.show();

		menuList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				customDialog.cancel();
				customDialog.dismiss();
				if (position == 0) {
					playMusic(listPosition);
				} else if (position == 1) {
					setRing();
				} else if (position == 2) {
					showMusicInfo(listPosition);
				}
			}

		});
	}

	/**
	 * ��ʾ������ϸ��Ϣ
	 * 
	 * @param position
	 */
	private void showMusicInfo(int position) {
		Mp3Info mp3Info = mp3Infos.get(position);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.music_info_layout, null);
		((TextView) view.findViewById(R.id.tv_song_title)).setText(mp3Info
				.getTitle());
		((TextView) view.findViewById(R.id.tv_song_artist)).setText(mp3Info
				.getArtist());
		((TextView) view.findViewById(R.id.tv_song_album)).setText(mp3Info
				.getAlbum());
		((TextView) view.findViewById(R.id.tv_song_filepath)).setText(mp3Info
				.getUrl());
		((TextView) view.findViewById(R.id.tv_song_duration)).setText(MediaUtil
				.formatTime(mp3Info.getDuration()));
		((TextView) view.findViewById(R.id.tv_song_format)).setText(Contsant
				.getSuffix(mp3Info.getDisplayName()));
		((TextView) view.findViewById(R.id.tv_song_size)).setText(Contsant
				.formatByteToMB(mp3Info.getSize()) + "MB");
		new CustomDialog.Builder(HomeActivity.this).setTitle("������ϸ��Ϣ:")
				.setNeutralButton("ȷ��", null).setView(view).create().show();
	}

	/**
	 * ��������
	 */
	protected void setRing() {
		RadioGroup rg_ring = new RadioGroup(HomeActivity.this);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		rg_ring.setLayoutParams(params);
		// ��һ����ѡ��ť����������
		final RadioButton rbtn_ringtones = new RadioButton(HomeActivity.this);
		rbtn_ringtones.setText("��������");
		rg_ring.addView(rbtn_ringtones, params);
		// �ڶ�����ѡ��ť����������
		final RadioButton rbtn_alarms = new RadioButton(HomeActivity.this);
		rbtn_alarms.setText("��������");
		rg_ring.addView(rbtn_alarms, params);
		// ��������ѡ��ť��֪ͨ����
		final RadioButton rbtn_notifications = new RadioButton(
				HomeActivity.this);
		rbtn_notifications.setText("֪ͨ����");
		rg_ring.addView(rbtn_notifications, params);
		new CustomDialog.Builder(HomeActivity.this).setTitle("��������")
				.setView(rg_ring)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						dialog.dismiss();
						if (rbtn_ringtones.isChecked()) {
							try {
								// ������������
								setRingtone(listPosition);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (rbtn_alarms.isChecked()) {
							try {
								// ��������
								setAlarm(listPosition);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (rbtn_notifications.isChecked()) {
							try {
								// ����֪ͨ����
								setNotifaction(listPosition);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}).setNegativeButton("ȡ��", null).show();
	}

	/**
	 * ������ʾ��
	 * 
	 * @param position
	 */
	protected void setNotifaction(int position) {
		Mp3Info mp3Info = mp3Infos.get(position);
		File sdfile = new File(mp3Info.getUrl().substring(4));
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, false);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);

		Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
				.getAbsolutePath());
		Uri newUri = this.getContentResolver().insert(uri, values);
		RingtoneManager.setActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_NOTIFICATION, newUri);
		Toast.makeText(getApplicationContext(), "����֪ͨ�����ɹ���", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * ��������
	 * 
	 * @param position
	 */
	protected void setAlarm(int position) {
		Mp3Info mp3Info = mp3Infos.get(position);
		File sdfile = new File(mp3Info.getUrl().substring(4));
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, false);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);

		Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
				.getAbsolutePath());
		Uri newUri = this.getContentResolver().insert(uri, values);
		RingtoneManager.setActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_ALARM, newUri);
		Toast.makeText(getApplicationContext(), "�������������ɹ���", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * ������������
	 * 
	 * @param position
	 */
	protected void setRingtone(int position) {
		Mp3Info mp3Info = mp3Infos.get(position);
		File sdfile = new File(mp3Info.getUrl().substring(4));
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, true);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);

		Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
				.getAbsolutePath());
		Uri newUri = this.getContentResolver().insert(uri, values);
		RingtoneManager.setActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_RINGTONE, newUri);
		Toast.makeText(getApplicationContext(), "�������������ɹ���", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * �˷���ͨ�������б���λ������ȡmp3Info����
	 * 
	 * @param listPosition
	 */
	public void playMusic(int listPosition) {
		if (mp3Infos != null) {
			Mp3Info mp3Info = mp3Infos.get(listPosition);
			musicTitle.setText(mp3Info.getTitle()); // ������ʾ����
			Bitmap bitmap = MediaUtil.getArtwork(this, mp3Info.getId(),
					mp3Info.getAlbumId(), true, true);// ��ȡר��λͼ����ΪСͼ
			musicAlbum.setImageBitmap(bitmap); // ������ʾר��ͼƬ
			Intent intent = new Intent(HomeActivity.this, PlayerActivity.class); // ����Intent������ת��PlayerActivity
			// ���һϵ��Ҫ���ݵ�����
			intent.putExtra("title", mp3Info.getTitle());
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("artist", mp3Info.getArtist());
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("currentTime", currentTime);
			intent.putExtra("repeatState", repeatState);
			intent.putExtra("shuffleState", isShuffle);
			intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
			startActivity(intent);
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * �����ؼ������Ի���ȷ���˳�
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			new CustomDialog.Builder(HomeActivity.this)
					.setTitle(R.string.info)
					.setMessage(R.string.dialog_messenge)
					.setPositiveButton(R.string.confrim,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									exit();

								}
							}).setNeutralButton(R.string.cancel, null).show();
			return false;
		}
		return false;
	}

	// �Զ����BroadcastReceiver�����������Service�������Ĺ㲥
	public class HomeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MUSIC_CURRENT)) {
				// currentTime����ǰ���ŵ�ʱ��
				currentTime = intent.getIntExtra("currentTime", -1);
				musicDuration.setText(MediaUtil.formatTime(currentTime));
			} else if (action.equals(MUSIC_DURATION)) {
				duration = intent.getIntExtra("duration", -1);
			} else if (action.equals(UPDATE_ACTION)) {
				// ��ȡIntent�е�current��Ϣ��current����ǰ���ڲ��ŵĸ���
				listPosition = intent.getIntExtra("current", -1);
				if (listPosition >= 0) {
					musicTitle.setText(mp3Infos.get(listPosition).getTitle());
				}
			} else if (action.equals(REPEAT_ACTION)) {
				repeatState = intent.getIntExtra("repeatState", -1);
				switch (repeatState) {
				case isCurrentRepeat: // ����ѭ��
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_current_selector);
					shuffleBtn.setClickable(false);
					break;
				case isAllRepeat: // ȫ��ѭ��
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_all_selector);
					shuffleBtn.setClickable(false);
					break;
				case isNoneRepeat: // ���ظ�
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_none_selector);
					shuffleBtn.setClickable(true);
					break;
				}
			} else if (action.equals(SHUFFLE_ACTION)) {
				isShuffle = intent.getBooleanExtra("shuffleState", false);
				if (isShuffle) {
					isNoneShuffle = false;
					shuffleBtn
							.setBackgroundResource(R.drawable.shuffle_selector);
					repeatBtn.setClickable(false);
				} else {
					isNoneShuffle = true;
					shuffleBtn
							.setBackgroundResource(R.drawable.shuffle_none_selector);
					repeatBtn.setClickable(true);
				}
			}
		}

	}
}

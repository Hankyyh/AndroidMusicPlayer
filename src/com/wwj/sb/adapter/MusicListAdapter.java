package com.wwj.sb.adapter;


import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wwj.sb.activity.R;
import com.wwj.sb.domain.Mp3Info;
import com.wwj.sb.utils.MediaUtil;

/**
 * �Զ���������б�������
 * Ϊ�˷�����չ����Ϊ֮ǰû�п��ǵ���ʾר������
 * @author wwj
 *
 */
public class MusicListAdapter extends BaseAdapter{
	private Context context;		//�����Ķ�������
	private List<Mp3Info> mp3Infos;	//���Mp3Info���õļ���
	private Mp3Info mp3Info;		//Mp3Info��������
	private int pos = -1;			//�б�λ��
	

	
	/**
	 * ���캯��
	 * @param context	������
	 * @param mp3Infos  ���϶���
	 */
	public MusicListAdapter(Context context, List<Mp3Info> mp3Infos) {
		this.context = context;
		this.mp3Infos = mp3Infos;
	}

	@Override
	public int getCount() {
		return mp3Infos.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(convertView == null)
		{
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.music_list_item_layout, null);
			viewHolder.albumImage = (ImageView) convertView.findViewById(R.id.albumImage);
			viewHolder.musicTitle = (TextView) convertView.findViewById(R.id.music_title);
			viewHolder.musicArtist = (TextView) convertView.findViewById(R.id.music_Artist);
			viewHolder.musicDuration = (TextView) convertView.findViewById(R.id.music_duration);
			convertView.setTag(viewHolder);			//��ʾ��View���һ����������ݣ�
		} else {
			viewHolder = (ViewHolder)convertView.getTag();//ͨ��getTag�ķ���������ȡ����
		}
		mp3Info = mp3Infos.get(position);
		if(position == pos) {
			viewHolder.albumImage.setImageResource(R.drawable.item);
		} else {
			Bitmap bitmap = MediaUtil.getArtwork(context, mp3Info.getId(),mp3Info.getAlbumId(), true, true);
			if(bitmap == null) {
				viewHolder.albumImage.setImageResource(R.drawable.music5);
			} else {
				viewHolder.albumImage.setImageBitmap(bitmap);
			}
			
		}
		viewHolder.musicTitle.setText(mp3Info.getTitle());			//��ʾ����
		viewHolder.musicArtist.setText(mp3Info.getArtist());		//��ʾ������
		viewHolder.musicDuration.setText(MediaUtil.formatTime(mp3Info.getDuration()));//��ʾʱ��
		
		return convertView;
	}
	
	
	/**
	 * ����һ���ڲ���
	 * ������Ӧ�Ŀؼ�����
	 * @author wwj
	 *
	 */
	public class ViewHolder {
		//���пؼ���������
		public ImageView albumImage;	//ר��ͼƬ
		public TextView musicTitle;		//���ֱ���
		public TextView musicDuration;	//����ʱ��
		public TextView musicArtist;	//����������
	}
}

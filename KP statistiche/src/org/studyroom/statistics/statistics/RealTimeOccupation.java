package org.studyroom.statistics.statistics;

import java.util.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;

public class RealTimeOccupation extends RealTimeStatistic implements Observer {
	private final Map<String,Seat> seat=new HashMap<>();
	private final Map<String,IntValue> val=new TreeMap<>();
	private final List<IntValueChangedListener> valListeners=new LinkedList<>();
	public RealTimeOccupation(){
		super(false,true);
		for (String uri : Persistence.getInstance().getStudyRoomsURIs())
			val.put(uri,new IntValue(0,0));
	}
	@Override
	public String getName(){
		return "Occupazione attuale aule";
	}
	@Override
	public String getValuesLabel(){
		return "% posti occupati";
	}
	@Override
	public void onSeatStateChanged(SeatStateChangedEvent e){
		IntValue v=val.get(e.getStudyRoomURI());
		int f=v.getFull(), p=v.getPartial();
		seat.putIfAbsent(e.getSeatURI(),new Seat());
		Seat s=seat.get(e.getSeatURI());
		if (s.isFullyOccupied())
			f--;
		else if (s.isPartiallyOccupied())
			p--;
		s.setState(e.isSeatAvailable()?Seat.FREE:e.isSeatPartiallyAvailable()?Seat.PARTIAL:Seat.FULL);
		if (s.isFullyOccupied())
			f++;
		else if (s.isPartiallyOccupied())
			p++;
		v=new IntValue(f,p);
		val.put(e.getStudyRoomURI(),v);
		StudyRoom sr=Persistence.getInstance().getStudyRoom(e.getStudyRoomURI());
		notifyChange(sr,v,e.isInitEvent());
	}
	@Override
	public Map<String,Value> getValues(String srURI){
		Map<String,Value> m=new LinkedHashMap<>();
		if (val.containsKey(srURI)){
			StudyRoom sr=Persistence.getInstance().getStudyRoom(srURI);
			m.put(sr.getName(),getPercentValue(val.get(srURI),sr));
		}
		return m;
	}
	private void notifyChange(StudyRoom sr, IntValue v, boolean init){
		notifyValueChange(sr.getName(),getPercentValue(v,sr));
		if (!init)
			for (IntValueChangedListener l : valListeners)
				l.onValueChanged(sr.getURI(),v);
	}
	@Override
	protected void loadStatisticData(Map<String,Map<String,String>> data){}
	@Override
	protected Map<String,Map<String,String>> saveStatisticData(){
		return Collections.emptyMap();
	}
	void addIntValueListener(IntValueChangedListener l){
		valListeners.add(l);
	}
	void removeIntValueListener(IntValueChangedListener l){
		valListeners.remove(l);
	}
}

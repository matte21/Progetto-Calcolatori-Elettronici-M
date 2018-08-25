package org.studyroom.statistics.viewmodel;

import java.beans.*;
import java.util.*;

public interface IMainViewModel {
	//for generic view-model
	void addPropertyChangeListener(PropertyChangeListener l);
	void removePropertyChangeListener(PropertyChangeListener l);
	
	//graphic
	String getGraphicTitle();
	String getTilesLabel();
	List<String> getCategories();
	Map<String,List<Double>> getData();
	
	//menus
	static final String DEFAULT_SR="Tutte";
	Collection<String> getStudyRooms(String university);
	Collection<String> getUniversities();
	Collection<String> getStatistics();
	
	//actions
	void selectStatistic(String name);
	void selectStudyRoom(String name, String university);
	void selectVisualization(String visualization);
	
	void unbind();
}
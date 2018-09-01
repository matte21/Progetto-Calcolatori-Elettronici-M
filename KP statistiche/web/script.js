//var kSessionID
var ws;
function createWebSocket(){
	ws=new WebSocket("ws://"+window.location.hostname+":82"); 
	ws.onmessage=function(e){
		document.getElementById("cont").innerHTML=e.data;
	}
	ws.onclose=createWebSocket;
}
function selectStatistic(st){
	ws.send("statistic="+st);
}
function selectStudyRoom(sr){
	ws.send("studyRoom="+sr);
}

createWebSocket();
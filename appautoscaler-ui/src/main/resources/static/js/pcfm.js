var waitingImagePath = 'images/waiting.gif';
var headers = {"Content-Type": "application/json"};

function postService(urlName, args, callback, waitingDiv, outputDiv, isAsync, isAuth) {
    showWaiting(waitingDiv, "");
    var argsStr = JSON.stringify(args);
    $.ajax({url: urlName,
        data: argsStr,
        dataType: "json",
        success: function(data, textStatus) {
            clearDiv(waitingDiv);
            callback(data, textStatus, waitingDiv, outputDiv);
        },
        type: "POST",
        headers:headers,
        async: isAsync
    });
}

function getService(urlName, args, callback, waitingDiv, outputDiv, isAsync, isAuth) {
    showWaiting(waitingDiv, "");
    $.ajax({url: urlName,
        data: args,
        dataType: "json",
        success: function(data, textStatus) {
            clearDiv(waitingDiv);
            callback(data, textStatus, waitingDiv, outputDiv);
        },
        type: "GET",
        async: isAsync
    });
}

function showWaiting(divname, message) {
    $("#"+divname).text("").append("<img src='"+waitingImagePath+"'/>");
}

function clearDiv(divname) {
    $("#"+divname).text("");
}

function showMessage(msg) {
    $("#message").text(msg);
}

function doStartupSequence() {
	console.info("test1");
  renderHomePageFunc();
}

var renderHomePageFunc = function() {
    clearDiv("content");

}

function renderMessagesList(data, divname) {
	
	console.info("render div=" + divname);
	console.info("render data=" + data);
	//console.log(data["resources"]);
	
	var itemHtml = "<div class='panel panel-basic box-shadow-1 app-list'>";
	itemHtml += "<div class='panel-header'><div class='panel-title-alt'>Apps</div></div>";
	itemHtml += "<div id='popupContainer' ng-controller='AppCtrl' class='panel-body pan'>";
	itemHtml += "<table class='apps table table-data table-fixed table-transparent mbn table-clickable table-uniform'>";
	itemHtml +="<thead><tr class='row'>";
	
	itemHtml +="<th class='col-xs-7'>Name</th>";
	itemHtml +="<th class='col-xs-3'>Org</th>";
	itemHtml +="<th class='col-xs-3'>Space</th>";
	itemHtml +="<th class='col-xs-3'>Instances</th>";
	itemHtml +="<th class='col-xs-8'>Buildpack</th>";
	itemHtml +="<th class='col-xs-3'>Last Push</th>";
	itemHtml +="<th class='col-xs-2'>Restart</th>";
	itemHtml +="<th class='col-xs-2'>Rule</th>";
	itemHtml +="</tr></thead>";
	
	itemHtml +="<tbody>";
	
	
    $.each(data["resources"], function(index, item){
      // var itemHtml = "<tr><td>"+item["name"]+"</td><td>"+item["message"]+"</td></tr>";
      // $("#messagesList").append(itemHtml);
    	ent1 = item["entity"];
      	meta1 = item["metadata"];
    	itemHtml +="<tr class='app row'>";
    	spaceguid=ent1["space_guid"];
    	orgkey=spaceguid+"-org";
    	
    	if (ent1["name"] === "pcfdemo-cjd") {
    		console.log(ent1);
    		console.log (ent1["space_guid"])
    		console.log (ent1["service_bindings_url"])
    	}
    	//<span class="fa fa-circle indicator green"></span><span class="count">11 </span>
    	//STARTED
    	//STOPPED
    	//CRASHED
    	iconText ="green";
    	if (ent1["state"] === "STARTED") {
    		iconText="green";
    	}
    	else if (ent1["state"] === "STOPPED") {
    		iconText="gray";
    	}
    	else if (ent1["state"] === "CRASHED") {
    		iconText="red";
    	}
    		
    	itemHtml += "<td>"+ent1["name"]+ '&nbsp;<span class="app-health-indicator"><span class="fa fa-circle indicator ' + iconText + '"></span><span class="count"></span></span></td><td>'+data[orgkey]+"</td><td>"+data[spaceguid]+ "</td><td>"+ent1["instances"]+"</td><td>"+ent1["detected_buildpack"]+"</td><td>"+ent1["package_updated_at"]+"</td><td>"+ getRestartButton() +"</td><td>"+"<img id='scaler-rule' src='images/autoscaler_icon.png' spaceGUID='" + ent1["space_guid"] + "' appGUID='" + ent1["service_bindings_url"] + "' alt='Smiley face' height='36' width='36'>"+"</td>";
    	itemHtml +="</tr>";
   });
		

    itemHtml +="</tbody>";

    itemHtml +="</table";
    itemHtml +="</div></div>";
	
	//$("#messagesList").append(itemHtml);
    $("#"+divname).append(itemHtml);
    
    $("#root").show();
    $(".loading-spinner").hide();
    

}

function getStopButton() {
	var txt = "<button class='stop-btn btn btn-default mrl tether-target tether-abutted tether-abutted-top tether-element-attached-bottom overlay-placement-top tether-element-attached-center overlay-placement-center' aria-describedby='stop-btn-tooltip'><div class='fa fa-stop'></div></button>";
	return txt;


	
}

function getRestartButton() {
	
	var txt = "<button class='restart-btn btn btn-default mrl' aria-describedby='restart-btn-tooltip'><div class='fa fa-repeat'></div></button>";
	return txt;
	
}

function saveMessage() {
    var name = $("#inputName").val();
    var message = $("#inputMessage").val();
    console.log("name: " + name + "; message: " + message);
    postService("/message",
        {"name":name, "message":message},
        renderHomePageFunc, "content", "content", true,  true);
}
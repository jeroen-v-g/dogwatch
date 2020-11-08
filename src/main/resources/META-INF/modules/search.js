var doRefresh;

function startRefreshCall(contextPath) {
	doRefresh = true;
	doRefreshCall(contextPath);
	document.getElementById("stopRefreshButton").style["display"] = "";

}

function doRefreshCall(contextPath) {
	var delayInMilliseconds = 5000;
	if (doRefresh) {
		doAjaxCall(contextPath);
		setTimeout(doRefreshCall , delayInMilliseconds, contextPath);
	}
}

function doAjaxCall(contextPath) {

	let xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
			document.getElementById("results").innerHTML = xhttp.responseText;
		}
	};
	xhttp.open("POST", contextPath + "/search:retrieve?action=refresh", true);
	xhttp.setRequestHeader("X-Requested-With", "XMLHttpRequest")
	xhttp.send();

	let xhttp2 = new XMLHttpRequest();
	xhttp2.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
			var response = JSON.parse(xhttp2.responseText);
			if (response.isSearching == false && doRefresh == true) {
				location.href=window.location.href;
			}
			if (response.isSearching == true) {
				doRefresh = true;
				document.getElementById("stopRefreshButton").style["display"] = "";
				document.getElementById("startRefreshButton").style["display"] = "none";
				document.getElementById("progressBar").style.width=response.percentageCompleted+"%";
				document.getElementById("progressBar").innerHTML=response.percentageCompleted+"%";
			}
		}
	};
	xhttp2.open("POST", contextPath + "/search:retrieve?action=isSearching",
			true);
	xhttp2.setRequestHeader("X-Requested-With", "XMLHttpRequest")
	xhttp2.send();
}

define(function() {
	return {

		startRefreshCall : function(contextPath) {
			startRefreshCall(contextPath);
		},

		doAjaxCall : function(contextPath) {
			doAjaxCall(contextPath);
		}
	}
});

function stopRefreshCall() {
	doRefresh = false;
	document.getElementById("stopRefreshButton").style["display"] = "none";
	document.getElementById("startRefreshButton").style["display"] = "";
}

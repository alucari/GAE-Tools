// ==UserScript==
// @name         Gtalk Status Notifier
// @namespace    http://silverwzw.com/
// @version      0.1
// @description  trace user status
// @match        https://mail.google.com/mail/*
// @copyright    2012+, Silverwzw
// @require      http://ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.js
// ==/UserScript==


function load(){
    var Log = [];
    var NicoleStatus = {"status":"initial","video":false,"android":false};
    jQuery.noConflict();
    setInterval(function () {
        var cs,csstr,d,ds,ss1="",ss2="",ns,nf,eq,ssattach,getStatus;
        eq = function (o1,o2) {
            return !((o1.status != o2.status) || (o1.android != o2.android) || (o1.video!=o2.video));
        };
        ssattach = function (s,a) {
            var ret;
            if (s == "") {
                ret = a;
            } else {
                ret = s + (a==""?"":(", " + a));
            }
            return ret;
        };
        getStatus = function () {
            var cList,contactList;
            var video = false;
            var android = false;
            var status = "unknown";
        
            contactList = jQuery("tr.M3aZbb");
            for (var i = 0; i < contactList.length; i++)
            {
                if (contactList[i].lastChild.firstElementChild.innerHTML == "¼ÖÜç")
                {
                    cList = contactList[i].firstElementChild.firstElementChild.classList;
                    break;
                }
            }
            if ((typeof cList) === "undefined") {
                return null;
            }
            if (cList.length == 4 && cList[1] == "dk") {
                video = true;
                if (cList[2] == "di") {
                    status = "away";
                }
                if (cList[2] == "dj") {
                    status = "busy";
                }
                if (cList[2] == "dh") {
                    status = "online";
                }
            }
            if (cList.length == 3) {
                if (cList[1] == "c6") {
                    status = "online";
                }
                if (cList[1] == "c8") {
                    status = "busy";
                }
                if (cList[1] == "c9") {
                    status = "online";
                    android = true;
                }
                if (cList[1] == "da") {
                    status = "away";
                    android = true;
                }
                if (cList[1] == "db") {
                    status = "busy";
                    android = true;
                }
                if (cList[1] == "dc") {
                    status = "away";
                }
                if (cList[1] == "df") {
                    status = "offline";
                }
            }
            return {"status":status,"video":video,"android":android};
        };
        cs = getStatus();
        if (cs == null) {
            return;
        }
        ns = NicoleStatus;
        NicoleStatus = cs;
        var fmt = function (i) {
            var ret;
            if (-10 < i && i < 10) {
                ret = "0" + i;
            } else {
                ret = "" + i;
            }
            return ret;
        };
        if (!eq(ns,cs)) {
            d = new Date();
            ds = fmt(d.getHours()) + ":" + fmt(d.getMinutes()) + ":" + fmt(d.getSeconds());
            if (ns.status != cs.status) {
                ss1 = ssattach(ss1, ns.status);
                ss2 = ssattach(ss2, cs.status);
            }
            if (ns.android != cs.android) {
                ss1 = ssattach(ss1, ns.android?"Mobile":"");
                ss2 = ssattach(ss2, cs.android?"Mobile":"");
            }
            if (ns.video != cs.video) {
                ss1 = ssattach(ss1, ns.video?"Video":"");
                ss2 = ssattach(ss2, cs.video?"Video":"");
            }
            Log[Log.length] = [ds,"["+ss1+"] => ["+ss2+"]"];
            nf = window.webkitNotifications.createNotification('', ds+" ["+ss1+"] => ["+ss2+"]", "[" + cs.status + (cs.video?", Video":"") + (cs.android?", Mobile":"") + "]");
            csstr = '?kind=NicoleLog&time=' + fmt(d.getYear()-100) + "-" + fmt(d.getMonth() + 1) + "-" + fmt(d.getDate()) + " " + ds + "&status=" + cs.status + (cs.video?",Video":"") + (cs.android?",Mobile":"") + "&initial=" +(ns.status=="initial"?"1":"0");
            GM_xmlhttpRequest({
                method: 'GET',
                url: ('http://tools.silverwzw.com/log' + csstr)
            });
            nf.onclick = function () {
                var l = "";
                for (var i = 0; i < Log.length; i++) {
                    var tmp;
                    tmp = Log[i][0] + " ### " + Log[i][1];
                    l += (l=="")?tmp:("\n"+tmp);
                }
                alert(l);
                console.log(l);
            };
            nf.show();
        }
    } ,1000);
}
load();
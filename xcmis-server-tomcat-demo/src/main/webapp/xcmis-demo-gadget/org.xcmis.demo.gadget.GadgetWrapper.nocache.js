function org_xcmis_demo_gadget_GadgetWrapper(){var l='',F='" for "gwt:onLoadErrorFn"',D='" for "gwt:onPropertyErrorFn"',n='"><\/script>',p='#',r='/',xb='1DBC63902966BD67564EF4F9F3DEF8C2.cache.html',wb='3140556D5193B6585F34F97168AB1112.cache.html',tb='4658C39EFC3A83A5DE7A08CEACA78B5E.cache.html',zb='72F2DE26A59CFF7646BF6B2ACE2B2F7C.cache.html',fc='<script defer="defer">org_xcmis_demo_gadget_GadgetWrapper.onInjectionDone(\'org.xcmis.demo.gadget.GadgetWrapper\')<\/script>',ic='<script id="',cc='<script language="javascript" src="',A='=',q='?',ub='BDBC247244534B87834BE5C76331D426.cache.html',C='Bad handler "',vb='C83A1D23E36118419814C3BF15240D1D.cache.html',ac='DOMContentLoaded',Ab='GadgetWrapper.css',o='SCRIPT',hc='__gwt_marker_org.xcmis.demo.gadget.GadgetWrapper',bc='applicationInitializer.js',ec='applicationInitializer.js"><\/script>',s='base',nb='begin',cb='bootstrap',u='clear.cache.gif',z='content',gc='end',mb='gecko',ob='gecko1_8',yb='gwt.hybrid',E='gwt:onLoadErrorFn',B='gwt:onPropertyErrorFn',y='gwt:property',Fb='head',rb='hosted.html?org_xcmis_demo_gadget_GadgetWrapper',Eb='href',lb='ie6',kb='ie8',ab='iframe',t='img',bb="javascript:''",Bb='link',qb='loadExternalRefs',v='meta',eb='moduleRequested',dc='moduleStartup',jb='msie',w='name',gb='opera',m='org.xcmis.demo.gadget.GadgetWrapper',db='position:absolute;width:0;height:0;border:none',Cb='rel',ib='safari',sb='selectingPermutation',x='startup',Db='stylesheet',pb='unknown',fb='user.agent',hb='webkit';var kc=window,k=document,jc=kc.__gwtStatsEvent?function(a){return kc.__gwtStatsEvent(a)}:null,Ec,uc,pc,oc=l,xc={},bd=[],Dc=[],nc=[],Ac,Cc;jc&&jc({moduleName:m,subSystem:x,evtGroup:cb,millis:(new Date()).getTime(),type:nb});if(!kc.__gwt_stylesLoaded){kc.__gwt_stylesLoaded={}}if(!kc.__gwt_scriptsLoaded){kc.__gwt_scriptsLoaded={}}function tc(){var b=false;try{b=kc.external&&(kc.external.gwtOnLoad&&kc.location.search.indexOf(yb)==-1)}catch(a){}tc=function(){return b};return b}
function wc(){if(Ec&&uc){var c=k.getElementById(m);var b=c.contentWindow;if(tc()){b.__gwt_getProperty=function(a){return qc(a)}}org_xcmis_demo_gadget_GadgetWrapper=null;b.gwtOnLoad(Ac,m,oc);jc&&jc({moduleName:m,subSystem:x,evtGroup:dc,millis:(new Date()).getTime(),type:gc})}}
function rc(){var j,h=hc,i;k.write(ic+h+n);i=k.getElementById(h);j=i&&i.previousSibling;while(j&&j.tagName!=o){j=j.previousSibling}function f(b){var a=b.lastIndexOf(p);if(a==-1){a=b.length}var c=b.indexOf(q);if(c==-1){c=b.length}var d=b.lastIndexOf(r,Math.min(c,a));return d>=0?b.substring(0,d+1):l}
;if(j&&j.src){oc=f(j.src)}if(oc==l){var e=k.getElementsByTagName(s);if(e.length>0){oc=e[e.length-1].href}else{oc=f(k.location.href)}}else if(oc.match(/^\w+:\/\//)){}else{var g=k.createElement(t);g.src=oc+u;oc=f(g.src)}if(i){i.parentNode.removeChild(i)}}
function Bc(){var f=document.getElementsByTagName(v);for(var d=0,g=f.length;d<g;++d){var e=f[d],h=e.getAttribute(w),b;if(h){if(h==y){b=e.getAttribute(z);if(b){var i,c=b.indexOf(A);if(c>=0){h=b.substring(0,c);i=b.substring(c+1)}else{h=b;i=l}xc[h]=i}}else if(h==B){b=e.getAttribute(z);if(b){try{Cc=eval(b)}catch(a){alert(C+b+D)}}}else if(h==E){b=e.getAttribute(z);if(b){try{Ac=eval(b)}catch(a){alert(C+b+F)}}}}}}
function ad(d,e){var a=nc;for(var b=0,c=d.length-1;b<c;++b){a=a[d[b]]||(a[d[b]]=[])}a[d[c]]=e}
function qc(d){var e=Dc[d](),b=bd[d];if(e in b){return e}var a=[];for(var c in b){a[b[c]]=c}if(Cc){Cc(d,a,e)}throw null}
var sc;function vc(){if(!sc){sc=true;var a=k.createElement(ab);a.src=bb;a.id=m;a.style.cssText=db;a.tabIndex=-1;k.body.appendChild(a);jc&&jc({moduleName:m,subSystem:x,evtGroup:dc,millis:(new Date()).getTime(),type:eb});a.contentWindow.location.replace(oc+Fc)}}
Dc[fb]=function(){var d=navigator.userAgent.toLowerCase();var b=function(a){return parseInt(a[1])*1000+parseInt(a[2])};if(d.indexOf(gb)!=-1){return gb}else if(d.indexOf(hb)!=-1){return ib}else if(d.indexOf(jb)!=-1){if(document.documentMode>=8){return kb}else{var c=/msie ([0-9]+)\.([0-9]+)/.exec(d);if(c&&c.length==3){var e=b(c);if(e>=6000){return lb}}}}else if(d.indexOf(mb)!=-1){var c=/rv:([0-9]+)\.([0-9]+)/.exec(d);if(c&&c.length==3){if(b(c)>=1008)return ob}return mb}return pb};bd[fb]={gecko:0,gecko1_8:1,ie6:2,ie8:3,opera:4,safari:5};org_xcmis_demo_gadget_GadgetWrapper.onScriptLoad=function(){if(sc){uc=true;wc()}};org_xcmis_demo_gadget_GadgetWrapper.onInjectionDone=function(){Ec=true;jc&&jc({moduleName:m,subSystem:x,evtGroup:qb,millis:(new Date()).getTime(),type:gc});wc()};rc();var Fc;if(tc()){if(kc.external.initModule&&kc.external.initModule(m)){kc.location.reload();return}Fc=rb}Bc();jc&&jc({moduleName:m,subSystem:x,evtGroup:cb,millis:(new Date()).getTime(),type:sb});if(!Fc){try{ad([kb],tb);ad([lb],ub);ad([gb],vb);ad([ib],wb);ad([ob],xb);ad([mb],zb);Fc=nc[qc(fb)]}catch(a){return}}var zc;function yc(){if(!pc){pc=true;if(!__gwt_stylesLoaded[Ab]){var a=k.createElement(Bb);__gwt_stylesLoaded[Ab]=a;a.setAttribute(Cb,Db);a.setAttribute(Eb,oc+Ab);k.getElementsByTagName(Fb)[0].appendChild(a)}wc();if(k.removeEventListener){k.removeEventListener(ac,yc,false)}if(zc){clearInterval(zc)}}}
if(k.addEventListener){k.addEventListener(ac,function(){vc();yc()},false)}var zc=setInterval(function(){if(/loaded|complete/.test(k.readyState)){vc();yc()}},50);jc&&jc({moduleName:m,subSystem:x,evtGroup:cb,millis:(new Date()).getTime(),type:gc});jc&&jc({moduleName:m,subSystem:x,evtGroup:qb,millis:(new Date()).getTime(),type:nb});if(!__gwt_scriptsLoaded[bc]){__gwt_scriptsLoaded[bc]=true;document.write(cc+oc+ec)}k.write(fc)}
org_xcmis_demo_gadget_GadgetWrapper();
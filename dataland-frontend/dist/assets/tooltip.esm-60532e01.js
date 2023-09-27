import{W as m,ar as y,$ as d,a6 as v,ac as b,a0 as a,ab as h}from"./index-17bd392c.js";var _=`
.p-tooltip {
    position:absolute;
    display:none;
    pointer-events:none;
    padding: .25em .5rem;
    max-width: 12.5rem;
}

.p-tooltip.p-tooltip-right,
.p-tooltip.p-tooltip-left {
    padding: 0 .25rem;
}

.p-tooltip.p-tooltip-top,
.p-tooltip.p-tooltip-bottom {
    padding:.25em 0;
}

.p-tooltip .p-tooltip-text {
    white-space: pre-line;
    word-break: break-word;
}

.p-tooltip-arrow {
    position: absolute;
    width: 0;
    height: 0;
    border-color: transparent;
    border-style: solid;
}

.p-tooltip-right .p-tooltip-arrow {
    margin-top: -.25rem;
    border-width: .25em .25em .25em 0;
}

.p-tooltip-left .p-tooltip-arrow {
    margin-top: -.25rem;
    border-width: .25em 0 .25em .25rem;
}

.p-tooltip.p-tooltip-top {
    padding: .25em 0;
}

.p-tooltip-top .p-tooltip-arrow {
    margin-left: -.25rem;
    border-width: .25em .25em 0;
}

.p-tooltip-bottom .p-tooltip-arrow {
    margin-left: -.25rem;
    border-width: 0 .25em .25rem;
}
`,$={root:"p-tooltip p-component",arrow:"p-tooltip-arrow",text:"p-tooltip-text"},O=m(_,{name:"tooltip",manual:!0}),w=O.load,E=y.extend({css:{classes:$,loadStyle:w}});function T(o,t){return x(o)||H(o,t)||L(o,t)||S()}function S(){throw new TypeError(`Invalid attempt to destructure non-iterable instance.
In order to be iterable, non-array objects must have a [Symbol.iterator]() method.`)}function L(o,t){if(o){if(typeof o=="string")return c(o,t);var e=Object.prototype.toString.call(o).slice(8,-1);if(e==="Object"&&o.constructor&&(e=o.constructor.name),e==="Map"||e==="Set")return Array.from(o);if(e==="Arguments"||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(e))return c(o,t)}}function c(o,t){(t==null||t>o.length)&&(t=o.length);for(var e=0,i=new Array(t);e<t;e++)i[e]=o[e];return i}function H(o,t){var e=o==null?null:typeof Symbol<"u"&&o[Symbol.iterator]||o["@@iterator"];if(e!=null){var i,l,n,r,u=[],s=!0,f=!1;try{if(n=(e=e.call(o)).next,t===0){if(Object(e)!==e)return;s=!1}else for(;!(s=(i=n.call(e)).done)&&(u.push(i.value),u.length!==t);s=!0);}catch(g){f=!0,l=g}finally{try{if(!s&&e.return!=null&&(r=e.return(),Object(r)!==r))return}finally{if(f)throw l}}return u}}function x(o){if(Array.isArray(o))return o}function p(o){"@babel/helpers - typeof";return p=typeof Symbol=="function"&&typeof Symbol.iterator=="symbol"?function(t){return typeof t}:function(t){return t&&typeof Symbol=="function"&&t.constructor===Symbol&&t!==Symbol.prototype?"symbol":typeof t},p(o)}var C=E.extend("tooltip",{beforeMount:function(t,e){var i,l,n,r=this.getTarget(t);if(r.$_ptooltipModifiers=this.getModifiers(e),e.value){if(typeof e.value=="string")r.$_ptooltipValue=e.value,r.$_ptooltipDisabled=!1,r.$_ptooltipEscape=!1,r.$_ptooltipClass=null,r.$_ptooltipFitContent=!0,r.$_ptooltipIdAttr=d()+"_tooltip",r.$_ptooltipShowDelay=0,r.$_ptooltipHideDelay=0;else if(p(e.value)==="object"&&e.value){if(v.isEmpty(e.value.value)||e.value.value.trim()==="")return;r.$_ptooltipValue=e.value.value,r.$_ptooltipDisabled=!!e.value.disabled===e.value.disabled?e.value.disabled:!1,r.$_ptooltipEscape=!!e.value.escape===e.value.escape?e.value.escape:!1,r.$_ptooltipClass=e.value.class||"",r.$_ptooltipFitContent=!!e.value.fitContent===e.value.fitContent?e.value.fitContent:!0,r.$_ptooltipIdAttr=e.value.id||d()+"_tooltip",r.$_ptooltipShowDelay=e.value.showDelay||0,r.$_ptooltipHideDelay=e.value.hideDelay||0}}else return;r.$_ptooltipZIndex=(i=e.instance.$primevue)===null||i===void 0||(i=i.config)===null||i===void 0||(i=i.zIndex)===null||i===void 0?void 0:i.tooltip,r.unstyled=((l=e.instance.$primevue)===null||l===void 0||(l=l.config)===null||l===void 0?void 0:l.unstyled)||((n=e.value)===null||n===void 0?void 0:n.unstyled)||!1,this.bindEvents(r,e),t.setAttribute("data-pd-tooltip",!0)},updated:function(t,e){var i,l,n=this.getTarget(t);if(n.$_ptooltipModifiers=this.getModifiers(e),this.unbindEvents(n),!!e.value){if(typeof e.value=="string")n.$_ptooltipValue=e.value,n.$_ptooltipDisabled=!1,n.$_ptooltipEscape=!1,n.$_ptooltipClass=null,n.$_ptooltipIdAttr=n.$_ptooltipIdAttr||d()+"_tooltip",n.$_ptooltipShowDelay=0,n.$_ptooltipHideDelay=0,this.bindEvents(n,e);else if(p(e.value)==="object"&&e.value)if(v.isEmpty(e.value.value)||e.value.value.trim()===""){this.unbindEvents(n,e);return}else n.$_ptooltipValue=e.value.value,n.$_ptooltipDisabled=!!e.value.disabled===e.value.disabled?e.value.disabled:!1,n.$_ptooltipEscape=!!e.value.escape===e.value.escape?e.value.escape:!1,n.$_ptooltipClass=e.value.class||"",n.$_ptooltipFitContent=!!e.value.fitContent===e.value.fitContent?e.value.fitContent:!0,n.$_ptooltipIdAttr=e.value.id||n.$_ptooltipIdAttr||d()+"_tooltip",n.$_ptooltipShowDelay=e.value.showDelay||0,n.$_ptooltipHideDelay=e.value.hideDelay||0,this.bindEvents(n,e);n.unstyled=((i=e.instance.$primevue)===null||i===void 0||(i=i.config)===null||i===void 0?void 0:i.unstyled)||((l=e.value)===null||l===void 0?void 0:l.unstyled)||!1}},unmounted:function(t,e){var i=this.getTarget(t);this.remove(i),this.unbindEvents(i,e),i.$_ptooltipScrollHandler&&(i.$_ptooltipScrollHandler.destroy(),i.$_ptooltipScrollHandler=null)},timer:void 0,methods:{bindEvents:function(t,e){var i=this,l=t.$_ptooltipModifiers;l.focus?(t.$_focusevent=function(n){return i.onFocus(n,e)},t.addEventListener("focus",t.$_focusevent),t.addEventListener("blur",this.onBlur.bind(this))):(t.$_mouseenterevent=function(n){return i.onMouseEnter(n,e)},t.addEventListener("mouseenter",t.$_mouseenterevent),t.addEventListener("mouseleave",this.onMouseLeave.bind(this)),t.addEventListener("click",this.onClick.bind(this))),t.addEventListener("keydown",this.onKeydown.bind(this))},unbindEvents:function(t){var e=t.$_ptooltipModifiers;e.focus?(t.removeEventListener("focus",t.$_focusevent),t.$_focusevent=null,t.removeEventListener("blur",this.onBlur.bind(this))):(t.removeEventListener("mouseenter",t.$_mouseenterevent),t.$_mouseenterevent=null,t.removeEventListener("mouseleave",this.onMouseLeave.bind(this)),t.removeEventListener("click",this.onClick.bind(this))),t.removeEventListener("keydown",this.onKeydown.bind(this))},bindScrollListener:function(t){var e=this;t.$_ptooltipScrollHandler||(t.$_ptooltipScrollHandler=new b(t,function(){e.hide(t)})),t.$_ptooltipScrollHandler.bindScrollListener()},unbindScrollListener:function(t){t.$_ptooltipScrollHandler&&t.$_ptooltipScrollHandler.unbindScrollListener()},onMouseEnter:function(t,e){var i=t.currentTarget,l=i.$_ptooltipShowDelay;this.show(i,e,l)},onMouseLeave:function(t){var e=t.currentTarget,i=e.$_ptooltipHideDelay;this.hide(e,i)},onFocus:function(t,e){var i=t.currentTarget,l=i.$_ptooltipShowDelay;this.show(i,e,l)},onBlur:function(t){var e=t.currentTarget,i=e.$_ptooltipHideDelay;this.hide(e,i)},onClick:function(t){var e=t.currentTarget,i=e.$_ptooltipHideDelay;this.hide(e,i)},onKeydown:function(t){var e=t.currentTarget,i=e.$_ptooltipHideDelay;t.code==="Escape"&&this.hide(t.currentTarget,i)},tooltipActions:function(t,e){if(!(t.$_ptooltipDisabled||!a.isExist(t))){var i=this.create(t,e);this.align(t),!t.unstyled&&a.fadeIn(i,250);var l=this;window.addEventListener("resize",function n(){a.isTouchDevice()||l.hide(t),window.removeEventListener("resize",n)}),this.bindScrollListener(t),h.set("tooltip",i,t.$_ptooltipZIndex)}},show:function(t,e,i){var l=this;i!==void 0?this.timer=setTimeout(function(){return l.tooltipActions(t,e)},i):this.tooltipActions(t,e)},tooltipRemoval:function(t){this.remove(t),this.unbindScrollListener(t)},hide:function(t,e){var i=this;clearTimeout(this.timer),e!==void 0?setTimeout(function(){return i.tooltipRemoval(t)},e):this.tooltipRemoval(t)},getTooltipElement:function(t){return document.getElementById(t.$_ptooltipId)},create:function(t,e){var i=t.$_ptooltipModifiers,l=a.createElement("div",{class:!t.unstyled&&this.cx("arrow"),style:{top:i!=null&&i.bottom?"0":i!=null&&i.right||i!=null&&i.left||!(i!=null&&i.right)&&!(i!=null&&i.left)&&!(i!=null&&i.top)&&!(i!=null&&i.bottom)?"50%":null,bottom:i!=null&&i.top?"0":null,left:i!=null&&i.right||!(i!=null&&i.right)&&!(i!=null&&i.left)&&!(i!=null&&i.top)&&!(i!=null&&i.bottom)?"0":i!=null&&i.top||i!=null&&i.bottom?"50%":null,right:i!=null&&i.left?"0":null},"p-bind":this.ptm("arrow",{context:i})}),n=a.createElement("div",{class:!t.unstyled&&this.cx("text"),"p-bind":this.ptm("text",{context:i})});t.$_ptooltipEscape?n.innerHTML=t.$_ptooltipValue:(n.innerHTML="",n.appendChild(document.createTextNode(t.$_ptooltipValue)));var r=a.createElement("div",{id:t.$_ptooltipIdAttr,role:"tooltip",style:{display:"inline-block",width:t.$_ptooltipFitContent?"fit-content":void 0},class:[!t.unstyled&&this.cx("root"),t.$_ptooltipClass],"p-bind":this.ptm("root",{context:i})},l,n);return document.body.appendChild(r),t.$_ptooltipId=r.id,this.$el=r,r},remove:function(t){if(t){var e=this.getTooltipElement(t);e&&e.parentElement&&(h.clear(e),document.body.removeChild(e)),t.$_ptooltipId=null}},align:function(t){var e=t.$_ptooltipModifiers;e.top?(this.alignTop(t),this.isOutOfBounds(t)&&(this.alignBottom(t),this.isOutOfBounds(t)&&this.alignTop(t))):e.left?(this.alignLeft(t),this.isOutOfBounds(t)&&(this.alignRight(t),this.isOutOfBounds(t)&&(this.alignTop(t),this.isOutOfBounds(t)&&(this.alignBottom(t),this.isOutOfBounds(t)&&this.alignLeft(t))))):e.bottom?(this.alignBottom(t),this.isOutOfBounds(t)&&(this.alignTop(t),this.isOutOfBounds(t)&&this.alignBottom(t))):(this.alignRight(t),this.isOutOfBounds(t)&&(this.alignLeft(t),this.isOutOfBounds(t)&&(this.alignTop(t),this.isOutOfBounds(t)&&(this.alignBottom(t),this.isOutOfBounds(t)&&this.alignRight(t)))))},getHostOffset:function(t){var e=t.getBoundingClientRect(),i=e.left+a.getWindowScrollLeft(),l=e.top+a.getWindowScrollTop();return{left:i,top:l}},alignRight:function(t){this.preAlign(t,"right");var e=this.getTooltipElement(t),i=this.getHostOffset(t),l=i.left+a.getOuterWidth(t),n=i.top+(a.getOuterHeight(t)-a.getOuterHeight(e))/2;e.style.left=l+"px",e.style.top=n+"px"},alignLeft:function(t){this.preAlign(t,"left");var e=this.getTooltipElement(t),i=this.getHostOffset(t),l=i.left-a.getOuterWidth(e),n=i.top+(a.getOuterHeight(t)-a.getOuterHeight(e))/2;e.style.left=l+"px",e.style.top=n+"px"},alignTop:function(t){this.preAlign(t,"top");var e=this.getTooltipElement(t),i=this.getHostOffset(t),l=i.left+(a.getOuterWidth(t)-a.getOuterWidth(e))/2,n=i.top-a.getOuterHeight(e);e.style.left=l+"px",e.style.top=n+"px"},alignBottom:function(t){this.preAlign(t,"bottom");var e=this.getTooltipElement(t),i=this.getHostOffset(t),l=i.left+(a.getOuterWidth(t)-a.getOuterWidth(e))/2,n=i.top+a.getOuterHeight(t);e.style.left=l+"px",e.style.top=n+"px"},preAlign:function(t,e){var i=this.getTooltipElement(t);i.style.left="-999px",i.style.top="-999px",a.removeClass(i,"p-tooltip-".concat(i.$_ptooltipPosition)),a.addClass(i,"p-tooltip-".concat(e)),i.$_ptooltipPosition=e},isOutOfBounds:function(t){var e=this.getTooltipElement(t),i=e.getBoundingClientRect(),l=i.top,n=i.left,r=a.getOuterWidth(e),u=a.getOuterHeight(e),s=a.getViewport();return n+r>s.width||n<0||l<0||l+u>s.height},getTarget:function(t){return a.hasClass(t,"p-inputwrapper")?a.findSingle(t,"input"):t},getModifiers:function(t){return t.modifiers&&Object.keys(t.modifiers).length?t.modifiers:t.arg&&p(t.arg)==="object"?Object.entries(t.arg).reduce(function(e,i){var l=T(i,2),n=l[0],r=l[1];return(n==="event"||n==="position")&&(e[r]=!0),e},{}):{}}}});export{C as T};
//# sourceMappingURL=tooltip.esm-60532e01.js.map

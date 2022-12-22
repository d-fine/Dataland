<#macro dala prefix>
    <#if social.providers??>
        <div id="kc-social-providers">
            <div class="d-separator">
                <div class="d-line"></div>
                <h4>OR</h4>
                <div class="d-line"></div>
            </div>
            <div>
                <#list social.providers as p>
                    <#if p.alias="linkedin">
                        <div id="social-linkedin">
                            <button
                                    class="p-button cursor-pointer font-semibold w-full p-button-linkedin uppercase flex justify-content-center align-items-center p-login-button"
                                    onclick="location.href='${p.loginUrl}'"
                            >
                                <img src="${url.resourcesPath}/in-white-21.png" alt="LinkedIn Logo" class="mr-2 mt-1 mb-1"/>
                                ${prefix} LINKEDIN
                            </button>
                        </div>
                    <#else>
                        <div id="social-${p.alias}">
                            <button
                                    class="p-button cursor-pointer font-semibold w-full bg-white text-primary uppercase mt-3 p-login-button"
                                    onclick="location.href='${p.loginUrl}'"
                            >
                                ${prefix} ${p.displayName!}
                            </button>
                        </div>
                    </#if>
                </#list>
            </div>
        </div>
    </#if>
</#macro>
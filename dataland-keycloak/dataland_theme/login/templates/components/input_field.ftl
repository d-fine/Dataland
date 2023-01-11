<#macro dala fieldName fieldErrorHandlers fieldHeading wrappingDivAttributes="" autofocus=false message=true additionalInputProperties...>
    <div class="input-group text-left mt-5 mb-5" ${wrappingDivAttributes?no_esc}>
        <input
                <#if messagesPerField.existsError(fieldErrorHandlers)>
                class="error"
                aria-invalid="true"
                </#if>
                id="${fieldName}"
                name="${fieldName}"
                <#if autofocus>autofocus</#if>
                required
                <#list additionalInputProperties as attrName, attrValue>
                ${attrName}="${attrValue}"
                </#list>
        />
        <label for="${fieldName}">${fieldHeading}</label>

        <#nested>

        <div
            class="input-error-container <#if !message || !messagesPerField.existsError(fieldErrorHandlers)>hidden</#if>"
        >
            <span class="material-icons-outlined input-error-icon">error</span>
            <div class="input-error-wrapper">
                <span class="input-error" aria-live="polite">
                     <#if message && messagesPerField.existsError(fieldErrorHandlers)>
                         ${kcSanitize(messagesPerField.getFirstError(fieldErrorHandlers))?no_esc}
                     </#if>
                </span>
            </div>
        </div>


    </div>
</#macro>

<#macro dala fieldName fieldErrorHandlers fieldHeading autofocus=false message=true additionalInputProperties...>
    <div class="input-group mt-5 mb-5 text-left">
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
        <label for="username">${fieldHeading}</label>

        <#if message && messagesPerField.existsError(fieldErrorHandlers)>
            <span class="material-icons-outlined input-error-icon">error</span>
            <div class="input-error-wrapper">
                <span class="input-error" aria-live="polite">
                    ${kcSanitize(messagesPerField.getFirstError(fieldErrorHandlers))?no_esc}
                </span>
            </div>
        </#if>
    </div>
</#macro>

<#macro dala fieldName fieldErrorHandlers fieldHeading autofocus=false message=true inputGroupMargin="mt-5 mb-5" additionalInputProperties...>
    <div class="input-group text-left ${inputGroupMargin}">
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
            id="input-error-${fieldName}"
            <#if !message || !messagesPerField.existsError(fieldErrorHandlers)>
            class="hidden"
            </#if>
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
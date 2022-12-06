<#import "dataland_template_tfa.ftl" as layout>
<@layout.registrationLayout displayRequiredFields=false displayMessage=!messagesPerField.existsError('totp','userLabel'); section>

    <#if section = "header">
        <div class="ml-3 mb-5">
            Two-factor authentication Setup
        </div>
    <#elseif section = "form">
        <ol class="ml-3 p-0 p-tfa-ordered-list" id="kc-totp-settings">

            <li class="font-semibold mb-4 p-tfa-ordered-list-item">
                <div class="p-3 pt-2 ml-3 p-tfa-instruction-box bg-white">
                    <p class="m-0 mb-3">${msg("loginTotpStep1")}</p>
                    <ul class="p-tfa-unordered-list-with-linebreak pl-0"
                        id="kc-totp-supported-apps">
                        <#list totp.supportedApplications as app>
                            <li class="flex align-items-center">
                                <img src="${url.resourcesPath}/check.svg">
                                &emsp;${msg(app)}
                            </li>
                        </#list>
                    </ul>
                </div>
            </li>


            <#if mode?? && mode = "manual">

                <li class="font-semibold mb-4 p-tfa-ordered-list-item">
                    <div class="p-3 pt-2 ml-3 p-tfa-instruction-box bg-white">
                        <p class="m-0">${msg("loginTotpManualStep2")}</p>
                        <p><span id="kc-totp-secret-key">${totp.totpSecretEncoded}</span></p>
                        <p class="flex flex-row-reverse m-0">
                            <a class="text-primary" href="${totp.qrUrl}"
                               id="mode-barcode">${msg("loginTotpScanBarcode")}</a>
                        </p>
                    </div>
                </li>

                <li class="font-semibold mb-4 p-tfa-ordered-list-item">
                    <div class="p-3 pt-2 ml-3 p-tfa-instruction-box bg-white">
                        <p class="m-0">${msg("loginTotpManualStep3")}</p>
                        <p>
                        <ul class="p-tfa-unordered-list pl-0">
                            <li id="kc-totp-type">
                                <span class="font-normal">${msg("loginTotpType")}:</span>
                                ${msg("loginTotp." + totp.policy.type)}
                            </li>
                            <li id="kc-totp-algorithm">
                                <span class="font-normal">${msg("loginTotpAlgorithm")}:</span>
                                ${totp.policy.getAlgorithmKey()}
                            </li>
                            <li id="kc-totp-digits">
                                <span class="font-normal">${msg("loginTotpDigits")}:</span>
                                ${totp.policy.digits}
                            </li>
                            <#if totp.policy.type = "totp">
                                <li id="kc-totp-period">
                                    <span class="font-normal">${msg("loginTotpInterval")}:</span>
                                    ${totp.policy.period}
                                </li>
                            <#elseif totp.policy.type = "hotp">
                                <li id="kc-totp-counter">
                                    <span class="font-normal">${msg("loginTotpCounter")}:</span>
                                    ${totp.policy.initialCounter}
                                </li>
                            </#if>
                        </ul>
                        </p>
                    </div>
                </li>



            <#else>

                <li class="font-semibold mb-4 p-tfa-ordered-list-item">
                    <div class="p-3 pt-2 ml-3 p-tfa-instruction-box bg-white">
                        <p class="m-0">${msg("loginTotpStep2")}</p>
                        <div class="flex">
                            <img id="kc-totp-secret-qr-code" src="data:image/png;base64, ${totp.totpSecretQrCode}"
                                 alt="Figure: Barcode">
                            <div class="w-full mb-5 flex flex-wrap align-content-end flex-row-reverse">
                                <a class="text-primary" href="${totp.manualUrl}"
                                   id="mode-manual">${msg("loginTotpUnableToScan")}</a>
                            </div>
                        </div>
                    </div>
                </li>

            </#if>


            <li class="font-semibold mb-4 p-tfa-ordered-list-item">

                <div class="w-11">
                    <div class="p-3 pt-2 pb-0 ml-3 p-tfa-instruction-box bg-white p-tfa-instruction-box-remove-border-bottom">

                        <p class="m-0">${msg("loginTotpStep3")}</p>
                        <p class="mb-0 p-tfa-text-devicename-color">${msg("loginTotpStep3DeviceName")}</p>

                    </div>


                    <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-totp-settings-form"
                          method="post">

                        <div class="p-3 pt-3 ml-3 mb-4 p-tfa-instruction-box bg-white p-tfa-instruction-box-remove-border-top">

                            <div class="${properties.kcInputWrapperClass!} p-tfa-input-width-limit">
                                <input type="text" class="${properties.kcInputClass!} p-input-grey-bottom-border" id="userLabel"
                                       name="userLabel"
                                       autocomplete="off"
                                       aria-invalid="<#if messagesPerField.existsError('userLabel')>true</#if>"
                                       placeholder="Device name<#if totp.otpCredentials?size gte 1>*</#if>"
                                />

                                <#if messagesPerField.existsError('userLabel')>
                                    <span id="input-error-otp-label" class="${properties.kcInputErrorMessageClass!} input-error"
                                          aria-live="polite">
                                     ${kcSanitize(messagesPerField.get('userLabel'))?no_esc}
                                </span>
                                </#if>
                            </div>

                        </div>


                        <div class="flex justify-content-between">


                            <div class="p-3 pt-2 ml-3 p-tfa-instruction-box bg-white">
                                <div class="${properties.kcInputWrapperClass!} p-tfa-text-one-time-code-title-size">
                                    <label for="totp" class="control-label">${msg("authenticatorCode")}</label>
                                    <span class="required p-tfa-text-asterisk-red">*</span>
                                </div>

                                <div class="${properties.kcInputWrapperClass!} pt-3 p-tfa-input-width-limit">
                                    <input type="text" id="totp" name="totp" autocomplete="off"
                                           class="${properties.kcInputClass!} p-input-grey-bottom-border"
                                           aria-invalid="<#if messagesPerField.existsError('totp')>true</#if>"
                                           placeholder="Enter the code"
                                    />

                                    <#if messagesPerField.existsError('totp')>
                                        <span id="input-error-otp-code" class="${properties.kcInputErrorMessageClass!} input-error"
                                              aria-live="polite">
                                            ${kcSanitize(messagesPerField.get('totp'))?no_esc}
                                    </span>
                                    </#if>
                                </div>


                                <input type="hidden" id="totpSecret" name="totpSecret" value="${totp.totpSecret}"/>
                                <#if mode??><input type="hidden" id="mode" name="mode" value="${mode}"/>
                                </#if>
                            </div>


                            <div class="flex align-items-end flex-row-reverse">
                                <#if isAppInitiatedAction??>
                                    <input type="submit"
                                           class="p-button font-semibold cursor-pointer uppercase ml-3"
                                           id="saveTOTPBtn" value="${msg("doSubmit")}"
                                    />
                                    <button type="submit"
                                            class="p-button font-semibold cursor-pointer bg-white text-primary uppercase"
                                            id="cancelTOTPBtn" name="cancel-aia" value="true"/>${msg("doCancel")}
                                    </button>
                                <#else>
                                    <input type="submit"
                                           class="p-button font-semibold cursor-pointer uppercase ml-3"
                                           id="saveTOTPBtn" value="${msg("doSubmit")}"
                                    />
                                </#if>
                            </div>

                        </div>

                    </form>

                </div>


            </li>
        </ol>



    </#if>
</@layout.registrationLayout>
<#import "dataland_template_full_width.ftl" as layout>
<@layout.registrationLayout displayRequiredFields=false displayMessage=!messagesPerField.existsError('totp','userLabel'); section>

    <#if section = "header">
        <div class="ml-3 mb-5">
            ${msg("loginTotpTitle")}
        </div>
    <#elseif section = "form">
        <ol class="ml-3 p-0 p-tfa-list" id="kc-totp-settings">

            <li class="bg-white font-semibold mb-4">
                <div class="p-3 ml-3 p-tfa-instruction-box">
                    <p class="m-0">${msg("loginTotpStep1")}</p>
                    <ul style="list-style-type:none; list-style-image: url('${url.resourcesPath}/img/check.svg')"
                        id="kc-totp-supported-apps">
                        <#list totp.policy.supportedApplications as app>
                            <li>${app}</li>
                        </#list>
                    </ul>
                </div>
            </li>


            <#if mode?? && mode = "manual">

                <li class="bg-white font-semibold mb-4">
                    <div class="p-3 ml-3 p-tfa-instruction-box">
                        <p class="m-0">${msg("loginTotpManualStep2")}</p>
                        <p><span id="kc-totp-secret-key">${totp.totpSecretEncoded}</span></p>
                        <p class="flex flex-row-reverse">
                            <a class="text-primary" href="${totp.qrUrl}"
                               id="mode-barcode">${msg("loginTotpScanBarcode")}</a>
                        </p>
                    </div>
                </li>

                <li class="bg-white font-semibold mb-4">
                    <div class="p-3 ml-3 p-tfa-instruction-box">
                        <p class="m-0">${msg("loginTotpManualStep3")}</p>
                        <p>
                        <ul style="list-style-type:none">
                            <li id="kc-totp-type">${msg("loginTotpType")}: ${msg("loginTotp." + totp.policy.type)}</li>
                            <li id="kc-totp-algorithm">${msg("loginTotpAlgorithm")}
                                : ${totp.policy.getAlgorithmKey()}</li>
                            <li id="kc-totp-digits">${msg("loginTogiotpDigits")}: ${totp.policy.digits}</li>
                            <#if totp.policy.type = "totp">
                                <li id="kc-totp-period">${msg("lnTotpInterval")}: ${totp.policy.period}</li>
                            <#elseif totp.policy.type = "hotp">
                                <li id="kc-totp-counter">${msg("loginTotpCounter")}: ${totp.policy.initialCounter}</li>
                            </#if>
                        </ul>
                        </p>
                    </div>
                </li>



            <#else>

                <li class="bg-white font-semibold mb-4">
                    <div class="p-3 ml-3 p-tfa-instruction-box">
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


            <li class="bg-white font-semibold mb-4">
                <div class="p-3 ml-3 p-tfa-instruction-box">

                    <p class="m-0">${msg("loginTotpStep3")}</p>
                    <p class="p-tfa-text-devicename">${msg("loginTotpStep3DeviceName")}</p>

                    <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-totp-settings-form"
                          method="post">


                        <div class="${properties.kcInputWrapperClass!} p-tfa-input-wrapper">
                            <input type="text" class="${properties.kcInputClass!} p-tfa-input-field" id="userLabel"
                                   name="userLabel"
                                   autocomplete="off"
                                   aria-invalid="<#if messagesPerField.existsError('userLabel')>true</#if>"
                                   placeholder="Device name<#if totp.otpCredentials?size gte 1>*</#if>"
                            />

                            <#if messagesPerField.existsError('userLabel')>
                                <span id="input-error-otp-label" class="${properties.kcInputErrorMessageClass!}"
                                      aria-live="polite">
                                     ${kcSanitize(messagesPerField.get('userLabel'))?no_esc}
                                </span>
                            </#if>

                        </div>


                        <div class="${properties.kcInputWrapperClass!}">
                            <label for="totp" class="control-label">${msg("authenticatorCode")}</label>
                            <span class="required p-tfa-asterisk-red">*</span>
                        </div>

                        <div class="${properties.kcInputWrapperClass!} p-tfa-input-wrapper">
                            <input type="text" id="totp" name="totp" autocomplete="off"
                                   class="${properties.kcInputClass!} p-tfa-input-field"
                                   aria-invalid="<#if messagesPerField.existsError('totp')>true</#if>"
                                   placeholder="Enter the code"
                            />

                            <#if messagesPerField.existsError('totp')>
                                <span id="input-error-otp-code" class="${properties.kcInputErrorMessageClass!}"
                                      aria-live="polite">
                            ${kcSanitize(messagesPerField.get('totp'))?no_esc}
                        </span>
                            </#if>
                        </div>


                        <input type="hidden" id="totpSecret" name="totpSecret" value="${totp.totpSecret}"/>
                        <#if mode??><input type="hidden" id="mode" name="mode" value="${mode}"/>
                        </#if>

                        <div class="w-full flex align-content-end flex-row-reverse">
                            <#if isAppInitiatedAction??>
                                <input type="submit"
                                       class="p-button cursor-pointer uppercase mt-3"
                                       id="saveTOTPBtn" value="${msg("doSubmit")}"
                                />
                                <button type="submit"
                                        class="p-button cursor-pointer bg-white text-primary uppercase mt-3"
                                        id="cancelTOTPBtn" name="cancel-aia" value="true"/>${msg("doCancel")}
                                </button>
                            <#else>
                                <input type="submit"
                                       class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                                       id="saveTOTPBtn" value="${msg("doSubmit")}"
                                />
                            </#if>
                        </div>


                    </form>


                </div>


            </li>
        </ol>



    </#if>
</@layout.registrationLayout>
<#import "template.ftl" as layout>
<#import "components/button/primary.ftl" as buttonPrimary>

<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "header">
        <#-- ${msg("loginTitleHtml",realm.name)} -->
    <#elseif section = "form">
        <!-- Form -->
        <form id="kc-totp-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <div class="flex justify-center"><span style="color: red; border-color: red; border-style: solid; padding: 4px 8px; border-width: 2px; border-radius: 4px">DEMO</span></div>
<#--                    <label for="totp" class="${properties.kcLabelClass!}">Please scan the code with the <b><a href="https://github.com/daniel-mader/selv-mobile/tree/dev" target="_blank" style="color: #0fc1b7; text-decoration: underline;">Selv app (modified)</a></b></label>-->
                </div>
            </div>

            <div style="display: flex; justify-content: center;">
                <img alt="qrcode" src="${qrcode!''}"/>
            </div>
            <div class="flex justify-center" style="font-size: 12px; color: gray; padding-bottom: 32px; margin-top: -24px;">
                <pre>${id!''}</pre>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                    </div>
                </div>

                <@buttonPrimary.kw name="login" type="submit" style="background-color: #0fc1b7">
                    <#-- ${msg("doLogIn")} -->
                    Continue
                </@buttonPrimary.kw>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <div class="${properties.kcFormButtonsWrapperClass!}">
                        <input type="hidden" id="id-hidden-input" name="credentialId"
                               <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                    </div>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>

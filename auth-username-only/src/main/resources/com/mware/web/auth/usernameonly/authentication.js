define([
    'flight/lib/component',
    './templates/login.hbs'
], function(
    defineComponent,
    template) {
    'use strict';

    return defineComponent(UserNameOnlyAuthentication);

    function UserNameOnlyAuthentication() {

        this.defaultAttrs({
            errorSelector: '.text-error',
            usernameSelector: '#username',
            rememberMeSelector: '#remember-me',
            loginButtonSelector: 'button'
        });

        this.after('initialize', function() {
            this.$node.html(template(this.attr));
            this.enableButton(false);

            this.on('click', {
                loginButtonSelector: this.onLoginButton
            });

            this.on('keydown keyup change paste', {
                usernameSelector: this.onUsernameChange
            });

            this.select('usernameSelector').focus();

            var match = window.location.hash.match(/^#username=(.*)/),
                username = match && match[1];
            if (username) {
                this.select('usernameSelector').val(username);
                this.select('loginButtonSelector').click();
            }

            let rememberedUser = localStorage.getItem('bc-username');

            if(rememberedUser) {
                this.select('usernameSelector').val(rememberedUser);
                this.select('rememberMeSelector').prop('checked', true);
                this.onUsernameChange();
            }
        });

        this.onUsernameChange = function(event) {
            var self = this,
                input = this.select('usernameSelector'),
                isValid = function() {
                    return $.trim(input.val()).length > 0;
                };

            if (event && event.which === $.ui.keyCode.ENTER) {
                event.preventDefault();
                event.stopPropagation();
                if (isValid() && event.type === 'keyup') {
                    return _.defer(this.login.bind(this));
                }
            }

            _.defer(function() {
                self.enableButton(isValid());
            });
        };

        this.onLoginButton = function(event) {
            event.preventDefault();
            event.stopPropagation();
            event.target.blur();

            this.login();
        };

        this.login = function() {
            var self = this,
                $error = this.select('errorSelector'),
                $username = this.select('usernameSelector'),
                $rememberMe = this.select('rememberMeSelector');

            if (this.disabled) {
                return;
            }

            this.enableButton(false, true);
            this.disabled = true;
            $error.empty();

            if($rememberMe.prop('checked')) {
                localStorage.setItem('bc-username', $username.val());
            } else {
                localStorage.removeItem('bc-username');
            }

            $.post('login', { username: $username.val() })
                .fail(function(xhr, status, error) {
                    $error.text(error);
                    self.disabled = false;
                    self.enableButton(true);
                })
                .done(function() {
                    self.trigger('loginSuccess');
                })
        };

        this.enableButton = function(enable, loading) {
            if (this.disabled) return;
            var button = this.select('loginButtonSelector');

            if (enable) {
                button.removeClass('loading').removeAttr('disabled');
            } else {
                button.toggleClass('loading', !!loading)
                    .attr('disabled', true);
            }
        }
    }

})

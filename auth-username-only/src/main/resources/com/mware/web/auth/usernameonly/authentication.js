/*
 * This file is part of the BigConnect project.
 *
 * Copyright (c) 2013-2020 MWARE SOLUTIONS SRL
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * MWARE SOLUTIONS SRL, MWARE SOLUTIONS SRL DISCLAIMS THE WARRANTY OF
 * NON INFRINGEMENT OF THIRD PARTY RIGHTS

 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the BigConnect software without
 * disclosing the source code of your own applications.
 *
 * These activities include: offering paid services to customers as an ASP,
 * embedding the product in a web application, shipping BigConnect with a
 * closed source product.
 */
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

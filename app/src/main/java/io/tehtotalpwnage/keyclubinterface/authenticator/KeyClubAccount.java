/*
 * Copyright (c) 2017 Michael Nguyen
 *
 * This file is part of KeyClubInterface.
 *
 * KeyClubInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeyClubInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KeyClubInterface.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.tehtotalpwnage.keyclubinterface.authenticator;

public class KeyClubAccount {
//    public static final String ACCOUNT_NAME = "KeyClubInterface";
    public static final String ACCOUNT_TYPE = "io.tehtotalpwnage.keyclubinterface";

    public static final String TOKEN_TYPE_ACCESS = "Access token";
    static final String TOKEN_TYPE_ACCESS_LABEL = "Authenticate the user with the server";

    public static final String TOKEN_TYPE_REFRESH = "Refresh token";
    static final String TOKEN_TYPE_REFRESH_LABEL = "Refresh credentials from the server";
}

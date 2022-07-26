/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.data.minecraft.loaders.forge;

import com.google.gson.annotations.SerializedName;

public class ATLauncherApiForgeVersion {
    public String version;
    public boolean recommended;

    @SerializedName("raw_version")
    public String rawVersion;

    @SerializedName("installer_sha1_hash")
    public String installerSha1Hash;

    @SerializedName("installer_size")
    public Long installerSize;

    @SerializedName("universal_sha1_hash")
    public String universalSha1Hash;

    @SerializedName("universal_size")
    public Long universalSize;

    @SerializedName("client_sha1_hash")
    public String clientSha1Hash;

    @SerializedName("client_size")
    public Long clientSize;

    @SerializedName("server_sha1_hash")
    public String serverSha1Hash;

    @SerializedName("server_size")
    public Long serverSize;
}

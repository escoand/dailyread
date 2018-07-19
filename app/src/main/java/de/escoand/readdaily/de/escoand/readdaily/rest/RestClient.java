/*
 * Copyright (c) 2018 escoand.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.escoand.readdaily.de.escoand.readdaily.rest;

import org.androidannotations.rest.spring.annotations.Accept;
import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.Rest;
import org.androidannotations.rest.spring.api.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

@Rest(converters = {MappingJackson2HttpMessageConverter.class, ByteArrayHttpMessageConverter.class})
public interface RestClient {

    @Get("http://h2652056.stratoserver.net/tmg_data/list.v2.json")
    @Accept(MediaType.APPLICATION_JSON)
    List<Product> getProductList();

    @Get("http://h2652056.stratoserver.net/tmg_data/img/{product}.png")
    @Accept(MediaType.IMAGE_PNG)
    byte[] getProductImage(@Path String product);

}

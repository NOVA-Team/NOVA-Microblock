/*
 * Copyright (c) 2017 NOVA, All rights reserved.
 * This library is free software, licensed under GNU Lesser General Public License version 3
 *
 * This file is part of NOVA.
 *
 * NOVA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NOVA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NOVA.  If not, see <http://www.gnu.org/licenses/>.
 */

package nova.microblock.wrapper.mc.forge.v1_7_10.launch;

import org.slf4j.Logger;

import nova.core.block.BlockManager;
import nova.core.event.bus.GlobalEvents;
import nova.core.game.ClientManager;
import nova.core.item.ItemManager;
import nova.core.loader.Loadable;
import nova.core.loader.Mod;
import nova.core.network.NetworkManager;
import nova.internal.core.tick.UpdateTicker;
import nova.microblock.NovaMicroblock;
import nova.microblock.injection.ComponentInjection;

/**
 * @author ExE Boss
 */
@Mod(id = "nova-microblock-wrapper", name = "Nova Microblock Wrapper", version = "0.0.1", novaVersion = "0.1.0", dependencies = { NovaMicroblock.MOD_ID + '@' + NovaMicroblock.VERSION + 'f'/*, "ForgeMultipart@1.2.xf" */})
public class NovaMicroblockWrapper implements Loadable {

	public final ComponentInjection componentInjection;
	public final ClientManager client;
	public final NetworkManager network;
	public final UpdateTicker.SynchronizedTicker ticker;
	public final ItemManager items;
	public final BlockManager blocks;
	public final Logger logger;
	public final GlobalEvents events;

	public NovaMicroblockWrapper(ComponentInjection componentInjection, ClientManager client, NetworkManager network, UpdateTicker.SynchronizedTicker ticker, ItemManager items, BlockManager blocks, GlobalEvents events, Logger logger) {
		this.componentInjection = componentInjection;
		this.client = client;
		this.network = network;
		this.ticker = ticker;
		this.items = items;
		this.blocks = blocks;
		this.logger = logger;
		this.events = events;
	}

}

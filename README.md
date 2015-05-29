[![Build Status](https://travis-ci.org/NOVAAPI/Microblock-Plugin.svg?branch=master)](https://travis-ci.org/NOVAAPI/Microblock-Plugin)
[![codecov.io](http://codecov.io/github/NOVAAPI/Microblock-Plugin/coverage.svg?branch=master)](http://codecov.io/github/NOVAAPI/Microblock-Plugin?branch=master)

# Microblock Plugin
Microblock Plugin is a NOVA Plugin that adds a microblock and multiblock system to NOVA. This API makes it easy to create microblocks and multiblocks.

## Usage
### Microblock
Add the Microblock component.

```java
class ExampleBlock extends Block {
	public ExampleBlock() {
		add(new Microblock(this, function));
	}
}

```

Where "function" is a callback you must provide to handle where your microblock should be placed.

### Multiblock
Add the Multiblock component. The multiblock component requires a collider to be attached.
Based on the size of the block collider, the multiblock will generate blocks to occupy space as to fill in required block space.

```java
class ExampleBlock extends Block {
	public ExampleBlock() {
		add(new Multiblock(this));
		add(new Collider(this));
	}
}

```

### Micro-Multi Blocks
A block can be both a microblock and and multiblock. This is probably the most used case.
Simple attach both the Multiblock and Microblock components to the block, and based on the collider,
microblock spaces and world block spaces will be occupied as needed. The amount of subdivions of a microblock
container is fixed, so there is a limit to how detailed the microblock can be.

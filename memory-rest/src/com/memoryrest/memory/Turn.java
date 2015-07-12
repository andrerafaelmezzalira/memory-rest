package com.memoryrest.memory;

final class Turn  {

	static final Turn first() {
		return new Turn(1000000);
	}

	final Turn next() {
		if (_next == null)
			_next = new Turn(0);
		return _next;
	}

	final synchronized void start() {
		if (_tickets == 0)
			try {
				this.wait();
			} catch (final InterruptedException e) {
				throw new RuntimeException("Unexpected Exception was thrown.",
						e);
			}
		_tickets--;
	}

	final void end() {
		next().haveSomeTickets(1);
	}

	final synchronized void haveSomeTickets(final int tickets) {
		if (_isAlwaysSkipped) {
			next().haveSomeTickets(tickets);
			return;
		}
		_tickets += tickets;
		notify();
	}

	Turn(int tickets) {
		_tickets = tickets;
	}

	Turn _next;
	int _tickets;
	boolean _isAlwaysSkipped;

}
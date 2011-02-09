package com.menny.android.anysoftkeyboard.keyboards;

import java.util.LinkedList;

public class KeyEventStateMachine {
	
	private final class KeyEventTransition {
		
		private KeyEventState next;
		private int keyCode;

		KeyEventTransition(int keyCode, KeyEventState next) {
			this.next = next;
			this.keyCode = keyCode;
		}
		
	}
		
	private final class KeyEventState {
		
		private LinkedList<KeyEventTransition> transitions;
		private int result;

		KeyEventState() {
			this.result = 0;
		}
		
		public KeyEventState getNext(int keyCode) {
			if (this.transitions == null)
				return null;
			for (KeyEventTransition transition: this.transitions) {
				if (transition.keyCode == keyCode) {
					return transition.next;
				}
			}
			return null;
		}

		public void addNextState(int keyCode, KeyEventState next) {
			if (this.transitions == null)
				this.transitions = new LinkedList<KeyEventTransition>();
			this.transitions.add(new KeyEventTransition(keyCode, next));
		}

		public void setCharacter(int result) {
			this.result = result;		
		}

		public boolean hasNext() {
			return (this.transitions != null);
		}
		
	}

	private KeyEventState start;

	public enum State { RESET, NOMATCH, PARTMATCH, FULLMATCH };
	
	private class NFAPart {
		
		KeyEventState state;
		int iVisibleSequenceLength;
		int iSequenceLength;
		private int resultChar;
		private int sequenceLength;
		private int visibleSequenceLength;
		
		NFAPart() {
			this.reset();
		}
		
		void reset() {
			this.state = KeyEventStateMachine.this.start;
			this.iSequenceLength = 0;
			this.iVisibleSequenceLength = 0;
		}
		
		public State addKeyCode(int keyCode) {
			this.state = this.state.getNext(keyCode);
			if (this.state == null) {
				this.reset();
				return State.RESET;
			} 			
			if (keyCode > 0)
				this.iVisibleSequenceLength++;
			this.iSequenceLength++;
					
			if (this.state.result != 0) {
				this.resultChar = this.state.result;
				this.sequenceLength = this.iSequenceLength;
				this.visibleSequenceLength = this.iVisibleSequenceLength;
				
				if (!this.state.hasNext()) {
					this.reset();
					return State.FULLMATCH;
				}			
				return State.PARTMATCH;
			}
			return State.NOMATCH;
		}
		
	}

	LinkedList<NFAPart> walker;
	
	private int sequenceLength;
	private int resultChar;
	
	public KeyEventStateMachine() {
		this.start = new KeyEventState();
		this.walker = new LinkedList<NFAPart>();
	}
	
	private KeyEventState addNextState(KeyEventState current, int keyCode) {
		KeyEventState next = current.getNext(keyCode);
		if (next != null)
			return next;
		next = new KeyEventState();
		current.addNextState(keyCode, next);
		return next; 
	}
	
	private KeyEventState addSpecialKeyNextState(KeyEventState current, int keyCode, int specialKey) {
		KeyEventState next = this.addNextState(current, keyCode);
		
		KeyEventState spnext = this.addNextState(current, specialKey);
		spnext.addNextState(keyCode, next);
		
		return next;
	}
	
	public void addSequence(int[] sequence, int result) {
		KeyEventState c = this.start;
		for (int i = 0; i < sequence.length; i++) {
			c = this.addNextState(c, sequence[i]);
		}
		c.setCharacter(result);
	}
	
	public void addSpecialKeySequence(int[] sequence, int specialKey, int result) {
		KeyEventState c = this.addNextState(this.start, specialKey);
		
		for (int i = 0; i < sequence.length; i++) {
			c = this.addSpecialKeyNextState(c, sequence[i], specialKey);
		}
		c.setCharacter(result);
	}

	public State addKeyCode(int keyCode) {
		this.sequenceLength = 0;
		this.resultChar = 0;
		
		if (this.walker.isEmpty())
			this.walker.add(new NFAPart());
		
		NFAPart found = null;
		State resultstate = State.RESET;
		
		LinkedList<NFAPart> proceded = new LinkedList<NFAPart>();
				
		for (NFAPart i = this.walker.poll(); i != null; i = this.walker.poll()) {
			State result = i.addKeyCode(keyCode);
			if (result == State.FULLMATCH) {
				if (found == null) { 
					resultstate = result;
					found = i;
					break;
				}
			}
			
			if (result == State.PARTMATCH || result == State.NOMATCH) {
				if (resultstate == State.RESET)
					resultstate = result;
				proceded.addLast(i);
			}
			if (result == State.PARTMATCH) {
				proceded.addLast(new NFAPart());
			} 
			if (result == State.PARTMATCH) {
				if ((found == null) || (found.sequenceLength < i.sequenceLength)) {
					found = i;
					resultstate = result;
				}
			}
		}
		this.walker = proceded;
		if (found != null) {
			this.sequenceLength = found.visibleSequenceLength;
			this.resultChar = found.resultChar;
			for (NFAPart i: this.walker) {
				if (found.visibleSequenceLength > 1) {
					i.iVisibleSequenceLength -= found.visibleSequenceLength-1;
				}
				if (i == found)
					break;
			}
		}
		return resultstate;
	}

	public int getCharacter() {
		return this.resultChar;
	}
	
	public int getSequenceLength() {
		return this.sequenceLength;
	}

	public void reset() {
		this.walker.clear();	
	}
	
}
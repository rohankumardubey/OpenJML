package stack;

public class StackImpl implements Stack {
		
	/*@ spec_public */ private int maxSize = 50;
	/*@ spec_public */ private int[] internalStack;
	/*@ spec_public */ private int stackCounter; //-RAC@ in count;
	//-RAC@ public represents count = stackCounter;
	//@ public invariant 0 <= stackCounter < maxSize;
	//@ public invariant internalStack.length == maxSize;
	
	@SuppressWarnings("unchecked")
	public StackImpl() {
		internalStack = new int[maxSize];
		stackCounter = 0;
	}
	
	//@ also ensures \result == stackCounter;
	//@ pure
	//@ helper
	public int count() {
		return stackCounter;
	}

	//@ pure
	public int itemAt(int i) {
		return internalStack[i-1];
	}

	//@ pure
	@Override public boolean isEmpty() {
		return internalStack.length == 0;
	}

	public boolean push(int item) {
		if(stackCounter + 1 > maxSize) return false;
		internalStack[stackCounter] = item;
		stackCounter++;
		return true;
	}

	public int top() {
		return internalStack[stackCounter-1];
	}

	public boolean remove() {
		if(stackCounter == 0) return false;
				stackCounter--;
		return true;
	}
	
	public static void main(String[] args) {
		Stack s = new StackImpl();
		s.push(2);
		s.push(2);
		s.push(2);
		System.out.println(s.itemAt(1));
		System.out.println(s.itemAt(2));
		System.out.println(s.itemAt(3));
	}

}

package org.drools;

import static org.drools.nvbj.List;
import static org.drools.nvbj.Map;
import static org.drools.nvbj.__;
import static org.drools.nvbj.println;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;


public class CollectionsTest extends TestCase {

	public void testList() {
		List<String> ls = List("a", "b", "c");
		assertEquals(3, ls.size());
		assertEquals("b", ls.get(1));
	}

	public void testMap() {
		println("this is less verbose");
		Map<String, String> mp = Map(
				__("name", "michael"),
				__("age", "42"));
		assertEquals(2, mp.size());
		assertEquals("michael", mp.get("name"));
		assertEquals("42", mp.get("age"));


	}

}

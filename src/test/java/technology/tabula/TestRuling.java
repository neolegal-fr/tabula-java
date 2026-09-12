package technology.tabula;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestRuling {
	
	Ruling ruling;
	
	@BeforeEach
	public void setUpRuling() {
		ruling = new Ruling(0, 0, 10, 10);
	}

	@Test
	public void testGetWidth() {
		assertEquals(10f, ruling.getWidth(), 1e-5);
	}

	@Test
	public void testGetHeight() {
		assertEquals(10f, ruling.getHeight(), 1e-5);
	}

	@Test
	public void testToString() {
		assertEquals("class technology.tabula.Ruling[x1=0.000000 y1=0.000000 x2=10.000000 y2=10.000000]",ruling.toString());
	}
	
	@Test
	public void testEqualsOther() {
		Ruling other = new Ruling(0, 0, 11, 10);
		assertTrue(ruling.equals(ruling));
	}
	
	@Test
	public void testEqualsDifferentInstance() {
		assertFalse(ruling.equals("test"));
	}
	
	@Test
	public void testNearlyIntersects(){
		Ruling another = new Ruling(0, 0, 11, 10);

		assertTrue(ruling.nearlyIntersects(another));
	}
	
	@Test
	public void testGetPositionError(){
		assertThrows(UnsupportedOperationException.class, () -> {
			Ruling other = new Ruling(0, 0, 1, 1);
			other.getPosition();
		});
	}
	
	@Test
	public void testSetPositionError(){
		assertThrows(UnsupportedOperationException.class, () -> {
			Ruling other = new Ruling(0, 0, 1, 1);
			other.setPosition(5f);
		});
	}
	
	@Test
	public void testsetPosition(){
		assertThrows(UnsupportedOperationException.class, () -> {
			ruling.setPosition(0);
		});
	}
	
	@Test
	public void testGetStartError(){
		assertThrows(UnsupportedOperationException.class, () -> {
			Ruling other = new Ruling(0, 0, 1, 1);
			other.getStart();
		});
	}
	
	@Test
	public void testGetEndError(){
		assertThrows(UnsupportedOperationException.class, () -> {
			Ruling other = new Ruling(0, 0, 1, 1);
			other.getEnd();
		});
	}
	
	@Test
	public void testParallelTo(){
		assertTrue(ruling.parallelTo(new Ruling(5, 5, 10, 10)));
		assertFalse(ruling.parallelTo(new Ruling(0, 10, 10, -10)));
		assertFalse(ruling.parallelTo(null));
		
		// the same ruling drawn in the opposite direction is still parallel
		Ruling downwards = new Ruling(new java.awt.geom.Point2D.Float(50f, 0f), new java.awt.geom.Point2D.Float(50f, 20f));
		Ruling upwards = new Ruling(new java.awt.geom.Point2D.Float(60f, 20f), new java.awt.geom.Point2D.Float(60f, 0f));
		assertTrue(downwards.parallelTo(upwards));
		assertTrue(upwards.parallelTo(downwards));
	}
	
	@Test
	public void testCollapseOrientedRulingsLeavesTheInputAlone(){
		Ruling first = new Ruling(10, 0, 0, 20);
		Ruling second = new Ruling(29, 0, 0, 20);
		List<Ruling> lines = new ArrayList<>(Arrays.asList(first, second));
		
		List<Ruling> collapsed = Ruling.collapseOrientedRulings(lines, 1, 2, 0.5f);
		
		assertEquals(1, collapsed.size());
		assertEquals(10f, collapsed.get(0).getStart(), 0.01);
		assertEquals(49f, collapsed.get(0).getEnd(), 0.01);
		// the rulings the caller handed us are still the ones it drew
		assertEquals(10f, first.getStart(), 0.01);
		assertEquals(30f, first.getEnd(), 0.01);
		assertEquals(29f, second.getStart(), 0.01);
	}
	
	@Test
	public void testCollapseOrientedRulingsMergesParallelRulingsWithinTheMagnetRadius(){
		// two vertical rulings 2 points apart, overlapping over most of their length
		Ruling left = new Ruling(0, 100, 0, 50);
		Ruling right = new Ruling(10, 102, 0, 50);
		List<Ruling> lines = new ArrayList<>(Arrays.asList(left, right));
		
		assertEquals(2, Ruling.collapseOrientedRulings(new ArrayList<>(lines)).size());
		assertEquals(2, Ruling.collapseOrientedRulings(new ArrayList<>(lines), 1, 2, 1f).size());
		
		List<Ruling> merged = Ruling.collapseOrientedRulings(new ArrayList<>(lines), 1, 2, 3f);
		assertEquals(1, merged.size());
		// both are 50 long, so the survivor sits halfway between them and spans them both
		assertEquals(101f, merged.get(0).getPosition(), 0.01);
		assertEquals(0f, merged.get(0).getStart(), 0.01);
		assertEquals(60f, merged.get(0).getEnd(), 0.01);
	}
	
	@Test
	public void testCollapseOrientedRulingsKeepsRulingsDrawnEndToEnd(){
		// tips touching but positions 2 points apart: two distinct borders, not one
		Ruling above = new Ruling(0, 100, 0, 30);
		Ruling below = new Ruling(30, 102, 0, 30);
		
		List<Ruling> collapsed = Ruling.collapseOrientedRulings(new ArrayList<>(Arrays.asList(above, below)), 1, 2, 3f);
		
		assertEquals(2, collapsed.size());
	}
	
	@Test
	public void testSetEndError(){
		assertThrows(UnsupportedOperationException.class, () -> {
			Ruling other = new Ruling(0, 0, 1, 1);
			other.setEnd(5f);
		});
	}
	
	
	@Test
	public void testColinear(){
//		Ruling another = new Ruling(0, 0, 500, 5);
		java.awt.geom.Point2D.Float float1 = new java.awt.geom.Point2D.Float(20, 20);
		java.awt.geom.Point2D.Float float2 = new java.awt.geom.Point2D.Float(0, 0);
		java.awt.geom.Point2D.Float float3 = new java.awt.geom.Point2D.Float(20, 0);
		java.awt.geom.Point2D.Float float4 = new java.awt.geom.Point2D.Float(0, 20);
		
		assertFalse(ruling.colinear(float1));
		assertTrue(ruling.colinear(float2));
		assertFalse(ruling.colinear(float3));
		assertFalse(ruling.colinear(float4));


	}

}

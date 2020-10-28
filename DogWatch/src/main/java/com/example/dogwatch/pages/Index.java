package com.example.dogwatch.pages;

import com.example.dogwatch.annotations.AnonymousAccess;

@AnonymousAccess
public class Index {

	public Object onActivate()
	{
		return Search.class;
	}
}

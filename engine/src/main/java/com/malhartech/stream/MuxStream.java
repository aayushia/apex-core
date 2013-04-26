/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.stream;

import com.malhartech.api.Sink;
import com.malhartech.engine.Stream;
import com.malhartech.engine.StreamContext;
import com.malhartech.util.CircularBuffer;
import java.lang.reflect.Array;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Chetan Narsude <chetan@malhar-inc.com>
 */
public class MuxStream implements Stream
{
  private HashMap<String, Sink<Object>> outputs = new HashMap<String, Sink<Object>>();
  @SuppressWarnings("VolatileArrayField")
  private volatile Sink<Object>[] sinks = NO_SINKS;

  /**
   *
   * @param context
   */
  @Override
  public void setup(StreamContext context)
  {
  }

  /**
   *
   */
  @Override
  public void teardown()
  {
    outputs.clear();
  }

  /**
   *
   * @param context
   */
  @Override
  public void activate(StreamContext context)
  {
    @SuppressWarnings("unchecked")
    Sink<Object>[] newSinks = (Sink<Object>[])Array.newInstance(Sink.class, outputs.size());

    int i = 0;
    for (final Sink<Object> s: outputs.values()) {
      newSinks[i++] = s;
    }
    sinks = newSinks;
  }

  /**
   *
   */
  @Override
  public void deactivate()
  {
    sinks = NO_SINKS;
  }

  /**
   *
   * @param id
   * @param sink
   */
  @Override
  public void setSink(String id, Sink<Object> sink)
  {
    if (sink == null) {
      outputs.remove(id);
      if (outputs.isEmpty()) {
        sinks = NO_SINKS;
      }
    }
    else {
      outputs.put(id, sink);
      if (sinks != NO_SINKS) {
        activate(null);
      }
    }
  }

  /**
   *
   * @param payload
   */
  @Override
  public void process(Object payload)
  {
    for (int i = sinks.length; i-- > 0;) {
      sinks[i].process(payload);
    }
  }

  @Override
  public boolean isMultiSinkCapable()
  {
    return true;
  }

  private static final Logger logger = LoggerFactory.getLogger(MuxStream.class);
}

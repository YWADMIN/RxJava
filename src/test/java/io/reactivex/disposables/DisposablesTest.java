/**
 * Copyright 2016 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.disposables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import io.reactivex.TestHelper;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;


public class DisposablesTest {

    @Test
    public void testUnsubscribeOnlyOnce() {
        Runnable dispose = mock(Runnable.class);
        Disposable subscription = Disposables.from(dispose);
        subscription.dispose();
        subscription.dispose();
        verify(dispose, times(1)).run();
    }

    @Test
    public void testEmpty() {
        Disposable empty = Disposables.empty();
        assertFalse(empty.isDisposed());
        empty.dispose();
        assertTrue(empty.isDisposed());
    }

    @Test
    public void testUnsubscribed() {
        Disposable disposed = Disposables.disposed();
        assertTrue(disposed.isDisposed());
    }
    
    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(Disposables.class);
    }
    
    @Test
    public void fromAction() {
        class AtomicAction extends AtomicBoolean implements Action {
            /** */
            private static final long serialVersionUID = -1517510584253657229L;

            @Override
            public void run() throws Exception {
                set(true);
            }
        }
        
        AtomicAction aa = new AtomicAction();
        
        Disposables.from(aa).dispose();
        
        assertTrue(aa.get());
    }
    
    @Test
    public void fromActionThrows() {
        try {
            Disposables.from(new Action() {
                @Override
                public void run() throws Exception {
                    throw new IllegalArgumentException();
                }
            }).dispose();
            fail("Should have thrown!");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        
        try {
            Disposables.from(new Action() {
                @Override
                public void run() throws Exception {
                    throw new InternalError();
                }
            }).dispose();
            fail("Should have thrown!");
        } catch (InternalError ex) {
            // expected
        }
        
        try {
            Disposables.from(new Action() {
                @Override
                public void run() throws Exception {
                    throw new IOException();
                }
            }).dispose();
            fail("Should have thrown!");
        } catch (RuntimeException ex) {
            if (!(ex.getCause() instanceof IOException)) {
                fail(ex.toString() + ": Should have cause of IOException");
            }
            // expected
        }

    }
    
    @Test
    public void disposeRace() {
        for (int i = 0; i < 100; i++) {
            final Disposable d = Disposables.empty();
            
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    d.dispose();
                }
            };
            
            TestHelper.race(r, r, Schedulers.io());
        }
    }
}
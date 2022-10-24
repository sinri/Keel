# BCrypt

> [Source on GitHub](https://github.com/promatik/jBCrypt/blob/master/src/main/java/org/mindrot/BCrypt.java)
> Original Author: Damien Miller
> Version 0.4 Referenced.

## Introduction

BCrypt implements OpenBSD-style Blowfish password hashing
using the scheme described in "A Future-Adaptable Password Scheme" by Niels Provos and David Mazieres.

This password hashing system tries to thwart off-line password cracking using a computationally-intensive hashing
algorithm,
based on Bruce Schneier's Blowfish cipher.
The work factor of the algorithm is parameterised, so it can be increased as computers get faster.

## Usage

Usage is really simple. To hash a password for the first time, call the hashpw method with a random salt, like this:

`String pw_hash = BCrypt.hashpw(plain_password, BCrypt.gensalt()); `

To check whether a plaintext password matches one that has been hashed previously, use the checkpw method:

```
if (BCrypt.checkpw(candidate_password, stored_hash))
    System.out.println("It matches");
else
    System.out.println("It does not match");
```

The `gensalt()` method takes an optional parameter (log_rounds) that determines the computational complexity of the
hashing:

```
String strong_salt = BCrypt.gensalt(10)<br />
String stronger_salt = BCrypt.gensalt(12)<br />
```

The amount of work increases exponentially (2**log_rounds), so each increment is twice as much work.
The default log_rounds is 10, and the valid range is 4 to 30.

## License

```
Copyright (c) 2006 Damien Miller <djm@mindrot.org>

Permission to use, copy, modify, and distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
```
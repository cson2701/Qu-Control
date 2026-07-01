# Qu Controller Issue Tracker

This file tracks the GitHub issues for the Qu Controller roadmap and keeps the implementation order explicit inside the repo.

## Current Priority

1. [#5 Build baseline desktop app with a single Main LR fader](https://github.com/cson2701/Qu-Control/issues/5)
2. [#21 Implement Qu hardware communication for the Main LR fader](https://github.com/cson2701/Qu-Control/issues/21)

These two issues are the first delivery path:

- `#5` establishes the baseline desktop app, one visible `Main LR` fader, and the controller abstraction.
- `#21` proves that the app can control the physical Qu mixer through that `Main LR` fader.

## Task List

| Status | Issue                                                   | Title                                                        | Notes                                  |
|--------|---------------------------------------------------------|--------------------------------------------------------------|----------------------------------------|
| Done | [#5](https://github.com/cson2701/Qu-Control/issues/5)   | Build baseline desktop app with a single Main LR fader       | First implementation task              |
| Todo   | [#21](https://github.com/cson2701/Qu-Control/issues/21) | Implement Qu hardware communication for the Main LR fader    | First hardware validation task         |
| Todo   | [#6](https://github.com/cson2701/Qu-Control/issues/6)   | Define shared domain models for channels and fader levels    | May be narrowed after `#5`             |
| Todo   | [#7](https://github.com/cson2701/Qu-Control/issues/7)   | Add mock MixerController for development without hardware    | Supports UI-first work                 |
| Todo   | [#8](https://github.com/cson2701/Qu-Control/issues/8)   | Build reusable vertical fader composable                     | Core UI building block                 |
| Todo   | [#9](https://github.com/cson2701/Qu-Control/issues/9)   | Render default 16-channel fader bank                         | Defer until after `#21`                |
| Todo   | [#10](https://github.com/cson2701/Qu-Control/issues/10) | Wire fader dragging to mock mixer state                      | Can be partially covered by `#5`       |
| Todo   | [#11](https://github.com/cson2701/Qu-Control/issues/11) | Display channel labels and current level values              | UI refinement                          |
| Todo   | [#12](https://github.com/cson2701/Qu-Control/issues/12) | Add screen state for visible and hidden channels             | Needed once more than one fader exists |
| Todo   | [#13](https://github.com/cson2701/Qu-Control/issues/13) | Support removing channels from the visible fader bank        | Later customization                    |
| Todo   | [#14](https://github.com/cson2701/Qu-Control/issues/14) | Support adding hidden channels back without duplicates       | Later customization                    |
| Todo   | [#15](https://github.com/cson2701/Qu-Control/issues/15) | Add reset action for the default 16-channel layout           | Depends on channel layout management   |
| Todo   | [#16](https://github.com/cson2701/Qu-Control/issues/16) | Add responsive desktop layout and connection status UI       | Useful after baseline flow exists      |
| Todo   | [#17](https://github.com/cson2701/Qu-Control/issues/17) | Introduce screen-level state holder for mixer actions        | Refactor once flow is proven           |
| Todo   | [#18](https://github.com/cson2701/Qu-Control/issues/18) | Add unit tests for fader state and channel visibility rules  | Add alongside shared logic growth      |
| Todo   | [#19](https://github.com/cson2701/Qu-Control/issues/19) | Define JVM mixer integration boundary for Qu16 communication | Likely absorbed by `#21` findings      |
| Todo   | [#20](https://github.com/cson2701/Qu-Control/issues/20) | Persist visible channel layout and connection settings       | Later usability task                   |
| Done | [#22](https://github.com/cson2701/Qu-Control/issues/22) | Add local skills to keep ISSUE_TRACKER.md in sync            | Repo workflow automation               |

## Suggested Execution Order

1. `#5` Build baseline desktop app with a single `Main LR` fader
2. `#21` Implement Qu hardware communication for the `Main LR` fader
3. `#8` Build reusable vertical fader composable
4. `#6` Define shared domain models for channels and fader levels
5. `#7` Add mock `MixerController` for development without hardware
6. `#16` Add responsive desktop layout and connection status UI
7. `#17` Introduce screen-level state holder for mixer actions
8. `#9` Render default 16-channel fader bank
9. `#11` Display channel labels and current level values
10. `#12` Add screen state for visible and hidden channels
11. `#13` Support removing channels from the visible fader bank
12. `#14` Support adding hidden channels back without duplicates
13. `#15` Add reset action for the default 16-channel layout
14. `#18` Add unit tests for fader state and channel visibility rules
15. `#20` Persist visible channel layout and connection settings

## Notes

- Update the `Status` column as work progresses.
- If `#21` reveals protocol or architecture constraints, revise the later issues rather than forcing the original plan.
- `#19` may become redundant once `#21` is implemented and documented.

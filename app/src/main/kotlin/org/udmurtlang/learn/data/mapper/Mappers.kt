package org.udmurtlang.learn.data.mapper

import org.udmurtlang.learn.data.local.db.DailyActivityEntity
import org.udmurtlang.learn.data.local.db.GameResultEntity
import org.udmurtlang.learn.data.local.db.ProgressEntity
import org.udmurtlang.learn.data.local.db.TopicEntity
import org.udmurtlang.learn.data.local.db.WordEntity
import org.udmurtlang.learn.domain.model.DailyActivity
import org.udmurtlang.learn.domain.model.GameResult
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.model.ProgressStatus
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.UserProgress
import org.udmurtlang.learn.domain.model.Word

fun TopicEntity.toDomain() = Topic(id, slug, titleUdm, titleRu, parseColor(color), emoji, sortOrder)

fun WordEntity.toDomain() = Word(id, topicId, udm, ru, transcription, emoji, exampleUdm, exampleRu)

fun ProgressEntity.toDomain() = UserProgress(
    wordId = wordId,
    status = status.toProgressStatus(),
    correctCount = correctCount,
    wrongCount = wrongCount,
    lastAnswerAt = lastAnswerAt,
)

fun GameResultEntity.toDomain() = GameResult(
    id = id,
    gameType = gameType.toGameType(),
    score = score,
    correct = correct,
    total = total,
    durationMs = durationMs,
    playedAt = playedAt,
)

fun DailyActivityEntity.toDomain() = DailyActivity(day, wordsLearned, gamesPlayed, correct, wrong)

fun String.toProgressStatus(): ProgressStatus = runCatching { ProgressStatus.valueOf(this) }
    .getOrDefault(ProgressStatus.NEW)

fun String.toGameType(): GameType = GameType.entries.firstOrNull { it.slug == this } ?: GameType.CHOICE

fun parseColor(hex: String): Long = hex.removePrefix("#").toLong(16) or 0xFF000000

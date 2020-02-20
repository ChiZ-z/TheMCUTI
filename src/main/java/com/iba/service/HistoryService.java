package com.iba.service;

import com.iba.model.chart.ChartItem;
import com.iba.model.chart.ResultStat;
import com.iba.model.history.History;
import com.iba.model.project.Project;
import com.iba.model.project.ProjectLang;
import com.iba.model.project.Term;
import com.iba.model.project.TermLang;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.repository.HistoryRepository;
import com.iba.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class HistoryService {

    private final HistoryRepository historyRepository;

    private final ProjectRepository projectRepository;

    private final ValidatorService validatorService;

    private static final List<Constants.StatType> constantStatTypeList = Arrays.asList(Constants.StatType.EDIT, Constants.StatType.TRANSLATE,
            Constants.StatType.AUTO_TRANSLATE, Constants.StatType.EDIT_BY_IMPORT, Constants.StatType.TRANSLATE_BY_IMPORT);

    @Autowired
    public HistoryService(HistoryRepository historyRepository, ProjectRepository projectRepository, ValidatorService validatorService) {
        this.historyRepository = historyRepository;
        this.projectRepository = projectRepository;
        this.validatorService = validatorService;
    }

    public List<History> setIsDisabled(List<History> historyList) {
        for (History history : historyList) {
            if (history.getProject().isDeleted()) {
                history.setDisabled(true);
            }
            if (history.getProjectLang() != null && history.getProjectLang().isDeleted()) {
                history.setDisabled(true);
            }
            if (history.getTerm() != null && history.getTerm().isDeleted()) {
                history.setDisabled(true);
            }
        }
        return historyList;
    }

    public void createTermEvent(Constants.StatType type, User user, Project project, Term term, String currentValue, String newValue) {
        History history = new History(user, project, type, term);
        switch (type) {
            case ADD_TERM:
            case EDIT_TERM: {
                history.setCurrentValue(currentValue);
                history.setNewValue(newValue);
                break;
            }
            case DELETE_TERM: {
                history.setCurrentValue(currentValue);
                break;
            }
        }
        historyRepository.save(history);
    }

    void addImportTermLangEvent(Map<String, String> termsMap, Project project, User user, List<History> historyList, ProjectLang projectLang, TermLang termLang) {
        if (termLang.getValue().equals("")) {
            historyList.add(new History(user, project, Constants.StatType.TRANSLATE_BY_IMPORT, termLang,
                    projectLang, historyList.get(0).getId(),
                    "", termsMap.get(termLang.getTerm().getTermValue())));
        } else {
            historyList.add(new History(user, project, Constants.StatType.EDIT_BY_IMPORT, termLang,
                    projectLang, historyList.get(0).getId(),
                    termLang.getValue(), termsMap.get(termLang.getTerm().getTermValue())));
        }
    }

    // TODO: 25.06.2019 Maybe remove some
    public void createProjectOrProjectLangEvent(Constants.StatType type, User user, Project project, ProjectLang projectLang) {
        History history = new History(user, project, type, projectLang);
        historyRepository.save(history);
    }

    public void createContributorEvent(Constants.StatType type, User user, Project project, User contributor) {
        History history = new History(user, project, contributor, type);
        historyRepository.save(history);
    }

    public List<History> createHistoryList(Long projectId, Long userId, List<Constants.StatType> statTypes, String start, String end) throws ParseException {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = formatter.parse(start);
        Date endDate = formatter.parse(end);
        return getHistoriesByDate(projectId, userId, statTypes, endDate, startDate);
    }

    private List<History> getHistoriesByDate(Long projectId, Long userId, List<Constants.StatType> statTypes, Date now, Date startDate) throws ParseException {
        if (userId != null && projectId == null && statTypes.contains(Constants.StatType.ALL)) {
            return historyRepository.findAllByUserIdAndDateBetween(userId, startDate, getNextRandomDate(now), constantStatTypeList);
        } else if (userId != null && projectId == null) {
            return historyRepository.findAllByActionAndUserIdAndDateBetween(statTypes, userId, startDate, getNextRandomDate(now), constantStatTypeList);
        } else if (userId == null && statTypes.contains(Constants.StatType.ALL)) {
            return historyRepository.findAllByProjectIdAndDateBetween(projectId, startDate, getNextRandomDate(now), constantStatTypeList);
        } else if (userId == null) {
            return historyRepository.findAllByActionAndProjectIdAndDateBetween(statTypes, projectId, startDate, getNextRandomDate(now), constantStatTypeList);
        } else if (statTypes.contains(Constants.StatType.ALL)) {
            return historyRepository.findAllByProjectIdAndUserIdAndDateBetween(projectId, userId, startDate, getNextRandomDate(now), constantStatTypeList);
        } else {
            return historyRepository.findAllByActionAndProjectIdAndUserIdAndDateBetween(statTypes, projectId, userId, startDate, getNextRandomDate(now), constantStatTypeList);
        }
    }

    /**
     * Create stat in project.
     *
     * @param type    - type of stat
     * @param user    - contributor
     * @param project - project
     */
    public void createStat(Constants.StatType type, User user, Project project, TermLang termLang, ProjectLang projectLang, String currentValue, String newValue, String refValue) {
        History history = new History(user, project, type, termLang, projectLang, currentValue, newValue, refValue);
        historyRepository.save(history);
    }

    /**
     * Edit stats.
     *
     * @param user         - id of authenticated User
     * @param currentValue - old value
     * @param newValue     - new value
     * @param project      - project id
     */
    public void simpleEdit(User user, Project project, TermLang termLang, ProjectLang projectLang, String currentValue, String newValue) {
        if (currentValue.equals("") && !newValue.equals("")) {
            createStat(Constants.StatType.TRANSLATE, user, project, termLang, projectLang, currentValue, newValue, null);
        } else if (!currentValue.equals("") && !currentValue.equals(newValue)) {
            createStat(Constants.StatType.EDIT, user, project, termLang, projectLang, currentValue, newValue, null);
        }
    }

    /**
     * Get all user stats.
     *
     * @param user - authenticated User
     * @return stats
     */
    @Transactional
    public ResultStat getAllUserStats(User user) {
        ResultStat resultStat = new ResultStat();
        List<Double> doubles = new ArrayList<>();
        Long count = projectRepository.countAllByAuthorAndIsDeletedFalse(user);
        resultStat.setProjectsCount(count);
        double differenceCoefficient = createDifferenceCoefficient(user);
        List<Long> longs = historyRepository.countByUserIdAndAction(user.getId(), Constants.StatType.AUTO_TRANSLATE);
        doubles.add(createCoefficient(longs, 150.0 * differenceCoefficient) * 0.06);
        resultStat.setAutoTranslateCount(getSum(longs));

        longs = historyRepository.countByUserIdAndAction(user.getId(), Constants.StatType.TRANSLATE);
        doubles.add(createCoefficient(longs, 30.0 * differenceCoefficient) * 0.5);
        resultStat.setTranslateCount(getSum(longs));

        longs = historyRepository.countByUserIdAndAction(user.getId(), Constants.StatType.EDIT);
        doubles.add(createCoefficient(longs, 60.0 * differenceCoefficient) * 0.3);
        resultStat.setEditCount(getSum(longs));

        longs = historyRepository.countByUserIdAndAction(user.getId(), Constants.StatType.EDIT_BY_IMPORT);
        doubles.add(createCoefficient(longs, 300.0 * differenceCoefficient) * 0.07);
        resultStat.setEditByImportCount(getSum(longs));

        longs = historyRepository.countByUserIdAndAction(user.getId(), Constants.StatType.TRANSLATE_BY_IMPORT);
        doubles.add(createCoefficient(longs, 300.0 * differenceCoefficient) * 0.07);
        resultStat.setTranslateByImportCount(getSum(longs));
        double sum = 0;
        for (double d : doubles) {
            sum += d;
        }
        resultStat.setRating(sum);
        return resultStat;
    }

    @Transactional
    public ResultStat getAllProjectStats(Project project) {
        ResultStat resultStat = new ResultStat();
        List<Long> longs = historyRepository.countByProjectIdAndAction(project.getId(), Constants.StatType.AUTO_TRANSLATE);
        resultStat.setAutoTranslateCount(getSum(longs));
        longs = historyRepository.countByProjectIdAndAction(project.getId(), Constants.StatType.TRANSLATE);
        resultStat.setTranslateCount(getSum(longs));
        longs = historyRepository.countByProjectIdAndAction(project.getId(), Constants.StatType.EDIT);
        resultStat.setEditCount(getSum(longs));
        longs = historyRepository.countByProjectIdAndAction(project.getId(), Constants.StatType.EDIT_BY_IMPORT);
        resultStat.setEditByImportCount(getSum(longs));
        longs = historyRepository.countByProjectIdAndAction(project.getId(), Constants.StatType.TRANSLATE_BY_IMPORT);
        resultStat.setTranslateByImportCount(getSum(longs));
        return resultStat;
    }

    /**
     * Count coefficients for stats
     *
     * @param user - authenticated User
     * @return coefficient
     */
    private double createDifferenceCoefficient(User user) {
        int contacts = Math.min(user.getContacts().size(), 3);
        int jobs = user.getJobs().size() > 2 ? 4 : 2 * user.getJobs().size();
        int langs = Math.min(user.getLangs().size(), 3);
        return 1.0 - (contacts + jobs + langs) * 0.02;
    }

    /**
     * Count coefficient for stats.
     *
     * @param diff  - difference
     * @param longs - list of coefficients
     * @return coefficient
     */
    private double createCoefficient(List<Long> longs, double diff) {
        if (longs.size() == 0) return 0.0;
        int div = 0;
        for (Long a : longs) {
            if (a > 10) div++;
        }
        if (div == 0) return 0;
        double average = (double) getSumForCoeff(longs) / (double) div;
        return average / (average + diff);
    }

    /**
     * Sum of coefficients
     *
     * @param longs - coefficients
     * @return sum
     */
    private Long getSumForCoeff(List<Long> longs) {
        long sum = 0;
        for (Long o : longs) {
            if (o > 10) sum += o;
        }
        return sum;
    }

    /**
     * Sum.
     *
     * @param longs - list of params
     * @return sum
     */
    private Long getSum(List<Long> longs) {
        long sum = 0;
        for (Long o : longs) {
            sum += o;
        }
        return sum;
    }

    /**
     * Get all user stats in project.
     *
     * @param user      - authenticated User
     * @param projectId - id of project
     * @return result stats
     */
    @Transactional
    public ResultStat getAllUserStatsInProject(User user, Long projectId) {
        ResultStat resultStat = new ResultStat();
        long count = historyRepository.countAllByUserIdAndActionAndProjectId(user.getId(), Constants.StatType.AUTO_TRANSLATE, projectId);
        resultStat.setAutoTranslateCount(count);
        count = historyRepository.countAllByUserIdAndActionAndProjectId(user.getId(), Constants.StatType.EDIT, projectId);
        resultStat.setEditCount(count);
        count = historyRepository.countAllByUserIdAndActionAndProjectId(user.getId(), Constants.StatType.TRANSLATE, projectId);
        resultStat.setTranslateCount(count);
        count = historyRepository.countAllByUserIdAndActionAndProjectId(user.getId(), Constants.StatType.EDIT_BY_IMPORT, projectId);
        resultStat.setEditByImportCount(count);
        count = historyRepository.countAllByUserIdAndActionAndProjectId(user.getId(), Constants.StatType.TRANSLATE_BY_IMPORT, projectId);
        resultStat.setTranslateByImportCount(count);
        return resultStat;
    }

    /**
     * Create item for chart.
     *
     * @param dateType - period of time
     * @param statType - type of stats
     * @return - chart item
     * @throws ParseException if parse date failed
     */
    @Transactional
    public ChartItem createChartItem(Long projectId, Long contributorId,
                                     Constants.StatType statType, String start, String end) throws ParseException {
        validatorService.validateRandomPeriodDate(start, end);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = formatter.parse(start);
        Date endDate = formatter.parse(end);
        if (startDate.compareTo(endDate) > 0) {
            Date buffer = startDate;
            startDate = endDate;
            endDate = buffer;
        }
        return createChartItemByPeriod(startDate, endDate, projectId, contributorId, statType);
    }

    /**
     * Parse period to custom period.
     *
     * @param start    - start of period of time
     * @param end      - end of period of time
     * @param statType - type of stats
     * @return ChartItem
     * @throws ParseException if parse Date failed
     */
    @Transactional
    public ChartItem createChartItemByPeriod(Date start, Date end, Long projectId, Long contributorId, Constants.StatType statType) throws ParseException {
        ChartItem item = new ChartItem();
        int days = daysBetween(start, end);
        item.setNodes(createPeriodDateList(end, days));
        switch (statType) {
            case ALL:
                item.setTranslatedStats(createPeriodStatList(projectId, contributorId, end, days, Constants.StatType.TRANSLATE));
                item.setAutoTranslatedStats(createPeriodStatList(projectId, contributorId, end, days, Constants.StatType.AUTO_TRANSLATE));
                item.setEditedStats(createPeriodStatList(projectId, contributorId, end, days, Constants.StatType.EDIT));
                item.setEditedByImportStats(createPeriodStatList(projectId, contributorId, end, days, Constants.StatType.EDIT_BY_IMPORT));
                item.setTranslatedByImportStats(createPeriodStatList(projectId, contributorId, end, days, Constants.StatType.TRANSLATE_BY_IMPORT));
                break;
            case TRANSLATE_BY_IMPORT:
                item.setTranslatedByImportStats(createPeriodStatList(projectId, contributorId, end, days, Constants.StatType.TRANSLATE_BY_IMPORT));
                break;
            case EDIT_BY_IMPORT:
                item.setEditedByImportStats(createPeriodStatList(projectId, contributorId, end, days, Constants.StatType.EDIT_BY_IMPORT));
                break;
            case EDIT:
                item.setEditedStats(createPeriodStatList(projectId, contributorId, end, days, Constants.StatType.EDIT));
                break;
            case TRANSLATE:
                item.setTranslatedStats(createPeriodStatList(projectId, contributorId, end, days, Constants.StatType.TRANSLATE));
                break;
            case AUTO_TRANSLATE:
                item.setAutoTranslatedStats(createPeriodStatList(projectId, contributorId, end, days, Constants.StatType.AUTO_TRANSLATE));
                break;
            case SUMMARY:
                item.setSummaryStats(createPeriodSummaryList(projectId, contributorId, end, days));
                break;
        }
        return item;
    }

    /**
     * Dif between days.
     *
     * @param d1 - day 1
     * @param d2 - day 2
     * @return dif
     */
    private int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    /**
     * Create list of stats by period.
     *
     * @param inputDate - first date
     * @param days      - number of days
     * @return list of stats
     * @throws ParseException if parse failed
     */
    @Transactional
    public List<Long> createPeriodStatList(Long projectId, Long contributorId, Date inputDate,
                                           int days, Constants.StatType type) throws ParseException {
        if (projectId == null && contributorId != null) {
            List<Long> longs = historyRepository.countAllByUserIdAndActionAndDateBetween(contributorId, type, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate));
            List<Date> dates = historyRepository.findByUserIdAndActionAndDateBetween(contributorId, type, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate));
            List<Date> datesFull = getDateListBetween(getPreviousRandomDate(inputDate, days), inputDate);
            return createFullList(longs, datesFull, dates);
        } else if (contributorId == null && projectId != null) {
            List<Long> longs = historyRepository.countAllByProjectIdAndActionAndDateBetween(projectId, type, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate));
            List<Date> dates = historyRepository.findByProjectIdAndActionAndDateBetween(projectId, type, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate));
            List<Date> datesFull = getDateListBetween(getPreviousRandomDate(inputDate, days), inputDate);
            return createFullList(longs, datesFull, dates);
        } else {
            List<Long> longs = historyRepository.countAllByUserIdAndProjectIdAndActionAndDateBetween(contributorId, projectId, type, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate));
            List<Date> dates = historyRepository.findByUserIdAndProjectIdAndActionAndDateBetween(contributorId, projectId, type, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate));
            List<Date> datesFull = getDateListBetween(getPreviousRandomDate(inputDate, days), inputDate);
            return createFullList(longs, datesFull, dates);
        }
    }

    /**
     * Create list of sum stats by period.
     *
     * @param inputDate - first date
     * @param days      - number of days
     * @return list of sum stats
     * @throws ParseException if parse failed
     */
    @Transactional
    public List<Long> createPeriodSummaryList(Long projectId, Long contributorId, Date inputDate,
                                              int days) throws ParseException {
        if (projectId == null && contributorId != null) {
            List<Long> longs = historyRepository.countAllByUserIdAndDateBetween(contributorId, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate), constantStatTypeList);
            List<Date> dates = historyRepository.findByUserIdAndDateBetween(contributorId, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate), constantStatTypeList);
            List<Date> datesFull = getDateListBetween(getPreviousRandomDate(inputDate, days), inputDate);
            return createFullList(longs, datesFull, dates);
        } else if (contributorId == null && projectId != null) {
            List<Long> longs = historyRepository.countAllByProjectIdAndDateBetween(projectId, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate), constantStatTypeList);
            List<Date> dates = historyRepository.findByProjectIdAndDateBetween(projectId, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate), constantStatTypeList);
            List<Date> datesFull = getDateListBetween(getPreviousRandomDate(inputDate, days), inputDate);
            return createFullList(longs, datesFull, dates);
        } else {
            List<Long> longs = historyRepository.countAllByUserIdAndProjectIdAndDateBetween(contributorId, projectId, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate), constantStatTypeList);
            List<Date> dates = historyRepository.findByUserIdAndProjectIdAndDateBetween(contributorId, projectId, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate), constantStatTypeList);
            List<Date> datesFull = getDateListBetween(getPreviousRandomDate(inputDate, days), inputDate);
            return createFullList(longs, datesFull, dates);
        }
    }

    /**
     * Create list period.
     *
     * @param inputDate - first date
     * @param days      - number of days
     * @return list of dates
     * @throws ParseException if parse failed
     */
    @Transactional
    public List<String> createPeriodDateList(Date inputDate, int days) throws ParseException {
        List<String> dates = new ArrayList<>();
        List<Date> dateList = getDateListBetween(getPreviousRandomDate(inputDate, days), getPreviousRandomDate(inputDate, 0));
        for (Date date : dateList) {
            dates.add(new SimpleDateFormat("yyyy-MM-dd").format(date.getTime()));
        }
        return dates;
    }

    /**
     * Get previous random date.
     *
     * @param date  - date
     * @param minus - number of minutes
     * @return Date
     * @throws ParseException if parse failed
     */
    private Date getPreviousRandomDate(Date date, int minus) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        return format.parse(localDate.minusDays(minus).toString());
    }


    /**
     * Get next random date.
     *
     * @param date - date
     * @return Date
     * @throws ParseException if parse failed
     */
    private Date getNextRandomDate(Date date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        return format.parse(localDate.plusDays(1).toString());
    }

    /**
     * Compare two dates.
     *
     * @param date1 - first date
     * @param date2 - second date
     * @return true if equal
     */
    private boolean dateCompareEqDays(Date date1, Date date2) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date1).equals(format.format(date2));
    }

    /**
     * Get list of date between two dates.
     *
     * @param d1 - first date
     * @param d2 - second date
     * @return list of date between
     * @throws ParseException if parse failed
     */
    private List<Date> getDateListBetween(Date d1, Date d2) throws ParseException {
        List<Date> dates = new ArrayList<>();
        while (d1.compareTo(d2) < 1) {
            dates.add(d1);
            d1 = getNextRandomDate(d1);
        }
        return dates;
    }

    /**
     * Get list of sum stats by period of time.
     *
     * @param fullDates     - list of full date
     * @param incomingDates - date for count ChartItems
     * @param longs         - list of stats
     * @return list of sum stats
     */
    private List<Long> createFullList(List<Long> longs, List<Date> fullDates, List<Date> incomingDates) {
        List<Long> result = new ArrayList<>();
        boolean isContains;
        int index = 0;
        for (Date date : fullDates) {
            isContains = false;
            for (Date d : incomingDates) {
                index = -1;
                if (dateCompareEqDays(date, d)) {
                    isContains = true;
                    index = incomingDates.indexOf(d);
                    break;
                }
            }
            if (isContains) {
                result.add(longs.get(index));
            } else result.add(0L);
        }
        return result;
    }
}
